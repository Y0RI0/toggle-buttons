# Toggle Buttons Plugin

Ever find yourself:

- struggling to remember plugin hotkeys?
- Having a hard time digging through your plugin list to find the toggle for the specific one you want to turn off/on?
- Wanting to just momentarily turn a plugin off without the mental overhead of finding it and turning it back on?
- Wish click-game was more click-button-thing-happen and less have-left-hand-on-keyboard-all-the-time?

Then this plugin's for you!

The idea behind this plugin is to create both clickable, configurable buttons to handle toggling
all your plugins on when you want them and off when you don't, from _within_ the game's window _or_
a "Stream Desk" like sidebar interface (at the very least to keep the ones you care about organized).

## Supported features:

- Ability to show and hide all buttons either with a config checkbox or hotkey
- Create plugin toggle buttons from the sidebar menu (Add Button +)
- Name your buttons so you can remember what they're for when you hover them in the sidebar
- Search game items and set them as your button icons
- move your buttons wherever you want in your game window, alt-drag or hard define their size
- lock them to runelite anchors, just like infoboxes (because they technically are)
- Many button shapes to choose from
- Pressed/unpressed button colors
- Local filesystem images for button icons
- Multiple plugins per button toggle
  - **(high-ish limit, don't blame me if you crash Runelite by toggling 50 at once)**
- "peek" feature, which allows the button to behave in the inverse way when pressed
  - IE. to "peek" show the Inventory Viewer while standing on your spells tab without leaving it always open
- Change default action of sidebar buttons, if you don't want visible game buttons, to execute the same toggle action as the window buttons without showing them, but allowing you to organize some toggles.

---

#### Creating a button

![Creating a button](assets/01_makebutton.gif)

#### Assigning plugins to a button

![Assigning plugins to a button](assets/02_assign_target.gif)

#### Peeking a plugin

![Peeking a plugin](assets/03_peek.gif)

#### Styling buttons

![Styling buttons](assets/04_Style.gif)

#### Custom image icons

![Custom image icons](assets/05_custom_image.gif)

---

## Not (never) supported features:

- this is not AHK, hotkeys, or simulation of any keypress stuff. Not allowed by Jagex, not allowed by Runelite. Don't ask please.
- Toggling other things besides plugin state
- Interacting much with the game view data, this plugin operates almost exclusively via overlays and native runelite APIs, mostly to keep it more stable over time and also to keep it less complex

---

Shout out to:

- [Station's Cozy Carts](https://github.com/StationEarthxo/Stations-Cozy-Carts) for having this toggleable cart button which made me realize this was possible to do in Runelite at all <3
- [Custom UI Anchors](https://github.com/Car-Role/Custom-UI-Anchors) for making this plugin's buttons way more "sticky"
- [Inventory Setups](https://github.com/dillydill123/inventory-setups) for the springboard idea of toggle-able plugins in the first place

