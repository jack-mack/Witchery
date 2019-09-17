package com.emoniph.witchery.client.gui;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.Minecraft;
import java.util.Hashtable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiButton;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import com.emoniph.witchery.network.PacketSyncMarkupBook;
import com.emoniph.witchery.Witchery;
import java.util.Iterator;
import net.minecraft.util.StatCollector;
import net.minecraft.item.Item;
import org.lwjgl.input.Keyboard;
import net.minecraft.nbt.NBTTagList;
import com.emoniph.witchery.util.NBT;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public class GuiScreenMarkupBook extends GuiScreen
{
   private static final ResourceLocation BACKGROUND;
   private final EntityPlayer player;
   private final ItemStack itemstack;
   private final int meta;
   private int updateCount;
   private int bookImageWidth;
   private int bookImageHeight;
   private GuiButtonNavigate buttonTopPage;
   private GuiButtonNavigate buttonPreviousPage;
   private GuiButtonNavigate buttonNextPage;
   private final List<String> pageStack;
   final List<Element> elements;
   private NextPage nextPage;

   public GuiScreenMarkupBook(final EntityPlayer player, final ItemStack itemstack) {
      this.bookImageWidth = 192;
      this.bookImageHeight = 192;
      this.pageStack = new ArrayList<String>();
      this.elements = new ArrayList<Element>();
      this.player = player;
      this.itemstack = itemstack;
      this.meta = ((itemstack != null) ? itemstack.getItemDamage() : 0);
      final NBTTagList nbtPageStack = NBT.get(itemstack).getTagList("pageStack", 8);
      for (int i = 0; i < nbtPageStack.tagCount(); ++i) {
         this.pageStack.add(nbtPageStack.getStringTagAt(i));
      }
   }

   public void updateScreen() {
      super.updateScreen();
      ++this.updateCount;
   }

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.constructPage();
   }

   private void constructPage() {
      final String page = (this.pageStack.size() > 0) ? this.pageStack.get(this.pageStack.size() - 1) : "toc";
      this.buttonList.clear();
      this.elements.clear();
      final byte b0 = 2;
      final int mid = (this.width - this.bookImageWidth) / 2;
      this.buttonList.add(this.buttonTopPage = new GuiButtonNavigate(1, mid + 120, b0 + 16, 2, GuiScreenMarkupBook.BACKGROUND));
      this.buttonList.add(this.buttonPreviousPage = new GuiButtonNavigate(2, mid + 34, b0 + 16, 1, GuiScreenMarkupBook.BACKGROUND));
      this.buttonList.add(this.buttonNextPage = new GuiButtonNavigate(3, mid + 120, b0 + 16, 0, GuiScreenMarkupBook.BACKGROUND));
      final String itemName = Item.itemRegistry.getNameForObject(this.itemstack.getItem());
      final String untranslated = itemName + "." + page;
      final StringBuilder markup = new StringBuilder(StatCollector.translateToLocal(untranslated));
      if (markup == null || markup.toString().equals(untranslated)) {
         return;
      }
      for (int i = 0; i < markup.length(); ++i) {
         final char c = markup.charAt(i);
         switch (c) {
            case '[': {
               this.elements.add(new Element());
               this.elements.get(this.elements.size() - 1).append(c);
               break;
            }
            case ']': {
               final Element e = this.elements.get(this.elements.size() - 1);
               if (e.tag.toString().equals("template")) {
                  final String templatePathRoot = Item.itemRegistry.getNameForObject(this.itemstack.getItem());
                  final String templatePath = templatePathRoot + "." + e.attribute;
                  final String template = StatCollector.translateToLocal(templatePath);
                  if (!template.isEmpty()) {
                     final String[] parms = e.text.toString().split("\\s");
                     final Object[] components = new Object[parms.length];
                     for (int j = 0; j < parms.length; ++j) {
                        final String[] kv = parms[j].split("=");
                        if (kv.length == 2) {
                           if (kv[0].matches("stack\\|\\d+")) {
                              final StringBuilder stackList = new StringBuilder();
                              for (final String stack : kv[1].split(",")) {
                                 stackList.append(String.format("[stack=%s]", stack));
                              }
                              final int index = Math.min(Integer.parseInt(kv[0].substring(kv[0].indexOf(124) + 1)), components.length - 1);
                              components[index] = stackList.toString();
                           }
                           else if (kv[0].matches("\\d+")) {
                              final int index2 = Math.min(Integer.parseInt(kv[0]), components.length - 1);
                              components[index2] = kv[1];
                           }
                        }
                     }
                     markup.insert(i + 1, String.format(template, components));
                     this.elements.remove(this.elements.size() - 1);
                  }
               }
               this.elements.add(new Element());
               break;
            }
            default: {
               if (this.elements.size() == 0) {
                  this.elements.add(new Element());
               }
               this.elements.get(this.elements.size() - 1).append(c);
               break;
            }
         }
      }
      this.nextPage = null;
      for (final Element element : this.elements) {
         final NextPage defaultNextPage = element.constructButtons(this.buttonList, this.itemstack);
         if (defaultNextPage != null) {
            this.nextPage = defaultNextPage;
         }
      }
      this.updateButtons();
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
      this.sendBookToServer();
   }

   private void updateButtons() {
      this.buttonNextPage.visible = (this.nextPage != null && this.nextPage.visible);
      this.buttonPreviousPage.visible = (this.pageStack.size() > 0);
      this.buttonTopPage.visible = (this.pageStack.size() > 0);
   }

   private void sendBookToServer() {
      if (this.player != null) {
         Witchery.packetPipeline.sendToServer(new PacketSyncMarkupBook(this.player.inventory.currentItem, this.pageStack));
      }
   }

   protected void actionPerformed(final GuiButton button) {
      if (button.enabled) {
         if (button.id == 0) {
            this.mc.displayGuiScreen(null);
         }
         else if (button.id == 1) {
            if (this.pageStack.size() > 0) {
               this.pageStack.remove(this.pageStack.size() - 1);
               for (int i = this.pageStack.size() - 1; i >= 0; --i) {
                  if (this.pageStack.get(i).startsWith("toc/")) {
                     break;
                  }
                  this.pageStack.remove(i);
               }
            }
            this.constructPage();
         }
         else if (button.id == 2) {
            if (this.pageStack.size() > 0) {
               this.pageStack.remove(this.pageStack.size() - 1);
               this.constructPage();
            }
         }
         else if (button.id == 3) {
            this.pageStack.add(this.nextPage.pageName);
            this.constructPage();
         }
         else if (button.id == 4) {
            this.pageStack.add(((GuiButtonUrl)button).nextPage);
            this.constructPage();
         }
         this.updateButtons();
      }
   }

   protected void keyTyped(final char par1, final int par2) {
      super.keyTyped(par1, par2);
   }

   public void drawScreen(final int mouseX, final int mouseY, final float par3) {
      GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
      this.mc.getTextureManager().bindTexture(GuiScreenMarkupBook.BACKGROUND);
      final int k = (this.width - this.bookImageWidth) / 2;
      final byte b0 = 2;
      this.drawTexturedModalRect(k, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
      final int maxWidth = 116;
      final int marginX = k + 36;
      this.buttonPreviousPage.xPosition = marginX;
      this.buttonPreviousPage.yPosition = 16;
      this.buttonTopPage.xPosition = k + this.bookImageWidth / 2 - this.buttonTopPage.width / 2 - 4;
      this.buttonTopPage.yPosition = 16;
      this.buttonNextPage.xPosition = k + this.bookImageWidth - this.buttonNextPage.width - 44;
      this.buttonNextPage.yPosition = 16;
      final int[] pos = { 0, 32 };
      final RenderState state = new RenderState(this.fontRendererObj, this.zLevel, mouseX, mouseY);
      for (final Element element : this.elements) {
         element.draw(pos, marginX, 116, state);
      }
      super.drawScreen(mouseX, mouseY, par3);
      if (state.tooltipStack != null) {
         this.renderToolTip(state.tooltipStack, mouseX, mouseY + 16);
      }
   }

   protected void renderToolTip(final ItemStack stack, final int x, final int y) {
      final List list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
      if (list != null) {
         final int power = WitcheryBrewRegistry.INSTANCE.getAltarPower(stack);
         if (power >= 0) {
            list.add(String.format(Witchery.resource("witchery.brewing.ingredientpowercost"), power, MathHelper.ceiling_double_int(1.4 * power)));
         }
      }
      for (int k = 0; k < list.size(); ++k) {
         if (k == 0) {
            list.set(k, stack.getRarity().rarityColor + (String)list.get(k));
         }
         else {
            list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
         }
      }
      final FontRenderer font = stack.getItem().getFontRenderer(stack);
      this.drawHoveringText(list, x, y, (font == null) ? this.fontRendererObj : font);
   }

   static {
      BACKGROUND = new ResourceLocation("witchery:textures/gui/bookSingle.png");
   }

   private static class NextPage
   {
      public final String pageName;
      public final boolean visible;

      public NextPage(final String attrib, final ItemStack book) {
         final int pipeIndex = attrib.indexOf(124);
         if (pipeIndex != -1) {
            this.pageName = attrib.substring(0, pipeIndex);
            this.visible = (book.getItemDamage() >= Integer.parseInt(attrib.substring(pipeIndex + 1)));
         }
         else {
            this.pageName = attrib;
            this.visible = true;
         }
      }
   }

   private static class Element
   {
      private final StringBuilder tag;
      private final StringBuilder attribute;
      private final StringBuilder text;
      private Capture capture;
      private static final String FORMAT_CHAR = "§";
      private static final String FORMAT_CLEAR = "§r";;
      private static final Hashtable<String, String> FORMATS;
      private GuiButtonUrl button;

      private Element() {
         this.tag = new StringBuilder();
         this.attribute = new StringBuilder();
         this.text = new StringBuilder();
         this.capture = Capture.TEXT;
      }

      @Override
      public String toString() {
         return String.format("tag=%s attribute=%s text=%s", this.tag, this.attribute, this.text);
      }

      public void append(final char c) {
         switch (c) {
            case '[': {
               this.capture = Capture.TAG;
               return;
            }
            case '=': {
               if (this.capture == Capture.TAG) {
                  this.capture = Capture.ATTRIB;
                  return;
               }
            }
            case '\t':
            case ' ': {
               if (this.capture == Capture.TAG || this.capture == Capture.ATTRIB) {
                  this.capture = Capture.TEXT;
                  return;
               }
               break;
            }
         }
         if (this.capture == Capture.TAG) {
            this.tag.append(c);
         }
         else if (this.capture == Capture.ATTRIB) {
            this.attribute.append(c);
         }
         else {
            this.text.append(c);
         }
      }

      private static Hashtable getFormats() {
         Hashtable formats = new Hashtable();
         formats.put("black", "§0");
         formats.put("darkblue", "§1");
         formats.put("darkgreen", "§2");
         formats.put("darkaqua", "§3");
         formats.put("darkred", "§4");
         formats.put("darkpurple", "§5");
         formats.put("darkyellow", "§6");
         formats.put("gray", "§7");
         formats.put("darkgray", "§8");
         formats.put("blue", "§9");
         formats.put("green", "§a");
         formats.put("aqua", "§b");
         formats.put("red", "§c");
         formats.put("purple", "§d");
         formats.put("yellow", "§e");
         formats.put("white", "§f");
         formats.put("b", "§l");
         formats.put("s", "§m");
         formats.put("u", "§n");
         formats.put("i", "§o");
         formats.put("h1", "§3§o");
         return formats;
      }

      public NextPage constructButtons(final List buttonList, final ItemStack stack) {
         final String tag = this.tag.toString();
         if (tag.equals("url")) {
            String attrib = this.attribute.toString();
            final int pipeIndex = attrib.indexOf(124);
            if (pipeIndex != -1) {
               attrib = attrib.substring(0, pipeIndex);
            }
            buttonList.add(this.button = new GuiButtonUrl(4, 0, 0, attrib, this.text.toString()));
         }
         else if (tag.equals("next")) {
            return new NextPage(this.attribute.toString(), stack);
         }
         return null;
      }

      public void draw(final int[] pos, final int marginX, final int maxWidth, final RenderState state) {
         GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
         final String tag = this.tag.toString();
         if (tag.equals("br")) {
            state.newline(pos);
            return;
         }
         if (tag.equals("tab")) {
            final int TAB_SPACE = 10;
            if (pos[0] + 10 > maxWidth) {
               state.newline(pos);
            }
            else {
               final int n = 0;
               pos[n] += 10;
            }
            return;
         }
         if (tag.equals("img")) {
            final String[] parms = this.attribute.toString().split("\\|");
            final int defaultWidth = 32;
            final String url = (parms.length > 0) ? parms[0] : "";
            final String halign = (parms.length > 1) ? parms[1] : "left";
            final String valign = (parms.length > 2) ? parms[2] : "top";
            final int width = (parms.length > 3) ? this.parseInt(parms[3], 32) : 32;
            final int height = (parms.length > 4) ? this.parseInt(parms[4], width) : width;
            if (!url.isEmpty()) {
               final ResourceLocation location = new ResourceLocation(url);
               Minecraft.getMinecraft().getTextureManager().bindTexture(location);
               if (halign.equals("right")) {
                  pos[0] = maxWidth - width;
               }
               else if (halign.equals("center")) {
                  pos[0] = maxWidth / 2 - width / 2;
               }
               if (pos[0] + width > maxWidth) {
                  state.newline(pos);
               }
               int y = pos[1];
               if (state.lineheight > height) {
                  if (valign.equals("bottom")) {
                     y += state.lineheight - height;
                  }
                  else if (valign.equals("middle")) {
                     y += state.lineheight / 2 - height / 2;
                  }
               }
               drawTexturedQuadFit(pos[0] + marginX, y, width, height, state.zLevel);
               final int n2 = 0;
               pos[n2] += width;
               state.adjustLineHeight(height);
            }
            return;
         }
         if (tag.equals("url")) {
            this.button.height = state.font.FONT_HEIGHT;
            this.button.width = state.font.getStringWidth(this.text.toString());
            if (pos[0] + this.button.width > maxWidth) {
               state.newline(pos);
            }
            final String[] parms = this.attribute.toString().split("\\|");
            final String url2 = (parms.length > 0) ? parms[0] : "";
            final String valign2 = (parms.length > 1) ? parms[1] : "top";
            this.button.xPosition = pos[0] + marginX;
            int y2 = pos[1];
            if (state.lineheight > this.button.height) {
               if (valign2.equals("bottom")) {
                  y2 += state.lineheight - this.button.height;
               }
               else if (valign2.equals("middle")) {
                  y2 += state.lineheight / 2 - this.button.height / 2;
               }
            }
            this.button.yPosition = y2;
            final int n3 = 0;
            pos[n3] += this.button.width;
            return;
         }
         if (tag.equals("locked")) {
            return;
         }
         if (tag.equals("stack")) {
            final String[] parms = this.attribute.toString().split("\\|");
            final String name = (parms.length > 0) ? parms[0] : "";
            int damage = 0;
            int size = 1;
            int offset = 1;
            if (parms.length > offset && parms[offset].matches("\\d+")) {
               damage = this.parseInt(parms[offset], 0);
               ++offset;
            }
            if (parms.length > offset && parms[offset].matches("\\d+")) {
               size = this.parseInt(parms[offset], 1);
               ++offset;
            }
            final String halign2 = (parms.length > offset) ? parms[offset] : "left";
            ++offset;
            final String valign3 = (parms.length > offset) ? parms[offset] : "top";
            if (!name.isEmpty()) {
               final boolean empty = name.equals("empty");
               final Item item = empty ? null : ((Item)Item.itemRegistry.getObject(name));
               final ItemStack stack = empty ? null : new ItemStack(item, size, damage);
               final int width2 = 18;
               final int height2 = 18;
               if (halign2.equals("right")) {
                  pos[0] = maxWidth - width2;
               }
               else if (halign2.equals("center")) {
                  pos[0] = maxWidth / 2 - width2 / 2;
               }
               if (pos[0] + width2 > maxWidth) {
                  state.newline(pos);
               }
               int y3 = pos[1];
               if (state.lineheight > height2) {
                  if (valign3.equals("bottom")) {
                     y3 += state.lineheight - height2;
                  }
                  else if (valign3.equals("middle")) {
                     y3 += state.lineheight / 2 - height2 / 2;
                  }
               }
               if (!empty) {
                  final RenderItem render = new RenderItem();
                  GL11.glPushMatrix();
                  GL11.glEnable(3042);
                  GL11.glBlendFunc(770, 771);
                  RenderHelper.enableGUIStandardItemLighting();
                  GL11.glEnable(32826);
                  GL11.glEnable(2929);
                  final int x = pos[0] + marginX;
                  render.renderItemAndEffectIntoGUI(state.font, Minecraft.getMinecraft().getTextureManager(), stack, x, y3);
                  render.renderItemOverlayIntoGUI(state.font, Minecraft.getMinecraft().getTextureManager(), stack, x, y3);
                  RenderHelper.disableStandardItemLighting();
                  GL11.glPopMatrix();
                  if (state.mouseX >= x && state.mouseY >= y3 && state.mouseX <= x + width2 && state.mouseY <= y3 + height2) {
                     state.tooltipStack = stack;
                  }
                  GL11.glDisable(2896);
               }
               final int n4 = 0;
               pos[n4] += width2;
               state.adjustLineHeight(height2);
               final String[] arr$;
               final String[] words = arr$ = this.text.toString().split("(?<=\\s)");
               for (final String word : arr$) {
                  final int textWidth = state.font.getStringWidth(word);
                  if (pos[0] + textWidth > maxWidth) {
                     state.newline(pos);
                     y3 = pos[1];
                  }
                  state.font.drawString(word, marginX + pos[0], y3 + (height2 - state.font.FONT_HEIGHT) / 2, 0);
                  final int n5 = 0;
                  pos[n5] += textWidth;
               }
            }
            return;
         }
         if (tag.equals("next")) {
            return;
         }
         final String preText = Element.FORMATS.containsKey(tag) ? Element.FORMATS.get(tag) : "";
         final String postText = Element.FORMATS.containsKey(tag) ? "§r" : "";
         final String[] arr$2;
         final String[] words2 = arr$2 = this.text.toString().split("(?<=\\s)");
         for (final String word2 : arr$2) {
            final int width3 = state.font.getStringWidth(word2);
            if (pos[0] + width3 > maxWidth) {
               state.newline(pos);
            }
            if (pos[0] != 0 || !word2.trim().isEmpty()) {
               state.font.drawString(preText + word2 + postText, marginX + pos[0], pos[1], 0);
               final int n6 = 0;
               pos[n6] += width3;
            }
         }
         if (tag.equals("h1")) {
            state.adjustLineHeight((int)Math.ceil(state.lineheight * 1.5f));
            state.newline(pos);
         }
      }

      private int parseInt(final String text, final int defaultValue) {
         try {
            return Integer.parseInt(text);
         }
         catch (NumberFormatException ex) {
            return defaultValue;
         }
      }

      public static void drawTexturedQuadFit(final double x, final double y, final double width, final double height, final double zLevel) {
         final Tessellator tessellator = Tessellator.instance;
         tessellator.startDrawingQuads();
         tessellator.addVertexWithUV(x + 0.0, y + height, zLevel, 0.0, 1.0);
         tessellator.addVertexWithUV(x + width, y + height, zLevel, 1.0, 1.0);
         tessellator.addVertexWithUV(x + width, y + 0.0, zLevel, 1.0, 0.0);
         tessellator.addVertexWithUV(x + 0.0, y + 0.0, zLevel, 0.0, 0.0);
         tessellator.draw();
      }

      static {
         FORMATS = getFormats();
      }

      private enum Capture
      {
         TAG,
         ATTRIB,
         TEXT
      }
   }

   private static class RenderState
   {
      final FontRenderer font;
      final float zLevel;
      final int mouseX;
      final int mouseY;
      ItemStack tooltipStack;
      int lineheight;

      public RenderState(final FontRenderer font, final float zLevel, final int mouseX, final int mouseY) {
         this.font = font;
         this.zLevel = zLevel;
         this.mouseX = mouseX;
         this.mouseY = mouseY;
         this.lineheight = font.FONT_HEIGHT;
      }

      public void newline(final int[] pos) {
         pos[0] = 0;
         final int n = 1;
         pos[n] += this.lineheight + 1;
         this.lineheight = this.font.FONT_HEIGHT;
      }

      public void adjustLineHeight(final int newHeight) {
         if (newHeight > this.lineheight) {
            this.lineheight = newHeight;
         }
      }
   }
}
