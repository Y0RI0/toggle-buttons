package com.togglebuttons;

/*
* Crafts the view of the plugin's sidebar panel,
* where buttons are managed in a grid and the
* selected button's settings are edited below it
* AKA, the file that made me realize why nobody learns java
* for UI work in 2026
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.AsyncBufferedImage;

@Singleton
class ToggleButtonsPluginPanel extends PluginPanel
{
	private static final String MODE_ENABLE_DISABLE = "Enable/disable";
	private static final String MODE_ON_OFF = "Turn on/off";
	private static final int GRID_COLUMNS = 4;
	private static final int CELL_SIZE = 44;

	private final ToggleButtonsToggle toggle;
	private final ToggleButtonsButtonStore store;
	private final ToggleButtonsIconSearch iconSearch;
	private final ItemManager itemManager;
	private final ColorPickerManager colorPickerManager;
	private final ToggleButtonsConfig config;

	private final JPanel buttonGrid = new JPanel(new GridLayout(0, GRID_COLUMNS, 4, 4));
	private final JPanel editorContainer = new JPanel();
	private final JButton addNewButton = new JButton("Add Button +");

	private String selectedId;
	private static final int menuPadding = 5;

	@Inject
	ToggleButtonsPluginPanel(
		ToggleButtonsToggle toggle,
		ToggleButtonsButtonStore store,
		ToggleButtonsIconSearch iconSearch,
		ItemManager itemManager,
		ColorPickerManager colorPickerManager,
		ToggleButtonsConfig config)
	{
		this.toggle = toggle;
		this.store = store;
		this.iconSearch = iconSearch;
		this.itemManager = itemManager;
		this.colorPickerManager = colorPickerManager;
		this.config = config;

		setLayout(new BorderLayout(0, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// Keep tooltips on screen long enough to read the longer ones
		ToolTipManager.sharedInstance().setDismissDelay(ToggleButtonsStyle.TOOLTIP_DISMISS_MS);

		// PluginPanel's internal scroll pane has no getter; find it in the wrapper
		for (Component c : getWrappedPanel().getComponents())
		{
			if (c instanceof JScrollPane)
			{
				final JScrollPane scrollPane = (JScrollPane) c;
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
				scrollBar.setUI(new ToggleButtonsScrollBarUI());
				scrollBar.setUnitIncrement(16);
			}
		}

		addNewButton.setToolTipText(ToggleButtonsStyle.tooltip("Adds a new clickable button to the sidebar and game window"));
		addNewButton.setBackground(ToggleButtonsStyle.PANEL_BACKGROUND);
		addNewButton.setForeground(ToggleButtonsStyle.TEXT_COLOR);
		addNewButton.addActionListener(e ->
		{
			final ToggleButtonsButton created = store.createButton();
			if (created != null)
			{
				selectedId = created.getId();
			}
			rebuild();
		});

		// Draw the header, plugin name add new button button
		final JPanel header = new JPanel(new BorderLayout(0, 5));
		header.setBorder(new EmptyBorder(5, 0, 0, 0)); // top,left,bottom,right
		final JLabel titleLabel = new JLabel("Toggle Buttons Plugin", SwingConstants.CENTER);
		titleLabel.setForeground(ToggleButtonsStyle.RUNESCAPE_YELLOW);
		final JPanel headerLabelRow = new JPanel(new BorderLayout());
		headerLabelRow.setBackground(ToggleButtonsStyle.PANEL_BACKGROUND);
		headerLabelRow.add(titleLabel);
		header.setBackground(ToggleButtonsStyle.PANEL_BACKGROUND);
		header.add(headerLabelRow);
		header.add(addNewButton, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		// Box layout grid styling
		final JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		final JPanel gridWrapper = new JPanel(new BorderLayout());
		gridWrapper.add(buttonGrid, BorderLayout.NORTH);
		gridWrapper.setBackground(ToggleButtonsStyle.PANEL_BACKGROUND);
		buttonGrid.setBackground(ToggleButtonsStyle.PANEL_BACKGROUND);
		gridWrapper.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ToggleButtonsStyle.PANEL_ACCENT),
			new EmptyBorder(6, 6, 6, 6)));
		content.add(gridWrapper);
		editorContainer.setLayout(new BorderLayout());
		content.add(editorContainer);
		add(content, BorderLayout.CENTER);

		rebuild();
	}

	@Override
	public void onActivate()
	{
		rebuild();
	}

	void rebuild()
	{
		final List<ToggleButtonsButton> buttons = store.getButtons();

		// Don't let users make an absurd amount of buttons and say something when
		// we get there
		final boolean atButtonLimit = buttons.size() >= ToggleButtonsButton.MAX_BUTTONS;
		addNewButton.setEnabled(!atButtonLimit);
		addNewButton.setToolTipText(atButtonLimit
			? ToggleButtonsStyle.tooltip("Maximum of " + ToggleButtonsButton.MAX_BUTTONS + " buttons reached")
			: ToggleButtonsStyle.tooltip("Adds a new clickable button to the sidebar and game window"));

		if (selectedId != null && buttons.stream().noneMatch(b -> selectedId.equals(b.getId())))
		{
			selectedId = null;
		}

		buttonGrid.removeAll();
		for (ToggleButtonsButton button : buttons)
		{
			buttonGrid.add(buildGridCell(button));
		}

		editorContainer.removeAll();
		final ToggleButtonsButton selected = buttons.stream()
			.filter(b -> b.getId().equals(selectedId))
			.findFirst()
			.orElse(null);
		if (selected != null)
		{
			editorContainer.add(buildEditor(selected), BorderLayout.NORTH);
		}

		revalidate();
		repaint();
	}

	private JButton buildGridCell(ToggleButtonsButton button)
	{
		// Tooltip resolves the name at hover time so renames show immediately
		final JButton cell = new JButton()
		{
			@Override
			public String getToolTipText(MouseEvent e)
			{
				return store.getButtons().stream()
					.filter(b -> b.getId().equals(button.getId()))
					.findFirst()
					.map(ToggleButtonsButton::getName)
					.orElse(button.getName());
			}
		};
		cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
		cell.setToolTipText(button.getName());
		cell.setFocusPainted(false);
		cell.setBackground(ToggleButtonsStyle.PANEL_ACCENT);

		if (button.getIconItemId() >= 0 || (button.getIconImagePath() != null && !button.getIconImagePath().isEmpty()))
		{
			// A local image takes priority over a searched item icon; never both
			final java.awt.image.BufferedImage fileImage = ToggleButtonsImageLoader.load(button.getIconImagePath());
			if (fileImage != null)
			{
				final java.awt.Image scaled = fileImage.getScaledInstance(CELL_SIZE - 8, CELL_SIZE - 8, java.awt.Image.SCALE_SMOOTH);
				cell.setIcon(new javax.swing.ImageIcon(scaled));
			}
			else if (button.getIconItemId() >= 0)
			{
				final AsyncBufferedImage img = itemManager.getImage(button.getIconItemId());
				img.addTo(cell);
			}
		}

		if (button.getId().equals(selectedId))
		{
			cell.setBorder(BorderFactory.createLineBorder(ToggleButtonsStyle.SELECTED_BORDER, 2));
		}

		if (config.sidebarClickMode() == ToggleButtonsSidebarClickMode.EXECUTE)
		{
			// Delegate to the same press/release logic as the game overlay so
			// behavior (including peek) is identical; look the button up fresh
			// from the buttonStore so edits apply without waiting for a rebuild
			cell.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					if (!SwingUtilities.isLeftMouseButton(e))
					{
						return;
					}

					store.getButtons().stream()
						.filter(b -> b.getId().equals(button.getId()))
						.findFirst()
						.ifPresent(toggle::press);
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					if (SwingUtilities.isLeftMouseButton(e))
					{
						toggle.release();
					}
				}
			});

			// In execute mode, editing moves to the right click menu
			final JPopupMenu popup = new JPopupMenu();
			final JMenuItem editItem = new JMenuItem("Edit button");
			editItem.addActionListener(e -> openEditor(button.getId()));
			popup.add(editItem);
			cell.setComponentPopupMenu(popup);
		}
		else
		{
			cell.addActionListener(e -> openEditor(button.getId()));
		}

		return cell;
	}

	private void openEditor(String buttonId)
	{
		selectedId = buttonId;
		rebuild();
	}

	private JPanel buildEditor(ToggleButtonsButton button)
	{
		final JPanel editor = new JPanel();
		// Sidebar Panel Border
		editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
		editor.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(ToggleButtonsStyle.PANEL_ACCENT),
				new EmptyBorder(6, 6, 6, 6)));

		// Name - Button Setting
		final JTextField nameField = new JTextField(button.getName());
		nameField.addActionListener(e -> saveName(button.getId(), nameField.getText()));
		nameField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				saveName(button.getId(), nameField.getText());
			}
		});
		editor.add(labeledRow("Name", nameField));
		editor.add(Box.createVerticalStrut(menuPadding));

		// Icon - Button Setting
		final JButton selectIconButton = new JButton("Icon");
		selectIconButton.setToolTipText(ToggleButtonsStyle.tooltip("Search for an item to use as the button icon (requires being logged in)"));
		selectIconButton.addActionListener(e -> iconSearch.open(itemId -> SwingUtilities.invokeLater(() -> updateButton(button.getId(), b -> b.setIconItemId(itemId)))));

		final JButton clearIconButton = new JButton("Clear icon");
		clearIconButton.addActionListener(e -> updateButton(button.getId(), b -> b.setIconItemId(-1)));

		final JPanel iconRow = new JPanel(new GridLayout(1, 2, 4, 0));
		iconRow.add(selectIconButton);
		iconRow.add(clearIconButton);
		editor.add(iconRow);
		editor.add(Box.createVerticalStrut(menuPadding));

		// Image - Button Setting; a local image takes priority over the item icon
		final JButton selectImageButton = new JButton("Local img");
		selectImageButton.setToolTipText(ToggleButtonsStyle.tooltip("Choose an image file from your computer to use as the button icon. Takes priority over a selected item icon."));
		selectImageButton.addActionListener(e ->
		{
			final JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select button image");
			chooser.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, gif, bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				// Copy into .runelite/toggle-buttons/ and persist the stored name
				final String storedName = ToggleButtonsImageLoader.store(button.getId(), chooser.getSelectedFile());
				if (storedName != null)
				{
					updateButton(button.getId(), b -> b.setIconImagePath(storedName));
				}
			}
		});

		final JButton clearImageButton = new JButton("Clear img");
		clearImageButton.addActionListener(e ->
		{
			ToggleButtonsImageLoader.delete(button.getIconImagePath());
			updateButton(button.getId(), b -> b.setIconImagePath(null));
		});

		final JPanel imageRow = new JPanel(new GridLayout(1, 2, 4, 0));
		imageRow.add(selectImageButton);
		imageRow.add(clearImageButton);
		editor.add(imageRow);
		editor.add(Box.createVerticalStrut(menuPadding));

		// Size - Button Setting
		final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(button.getWidth(), 16, 256, 1));
		widthSpinner.addChangeListener(e -> updateButton(button.getId(), b -> b.setWidth((int) widthSpinner.getValue()), false));
		editor.add(labeledRow("Width", widthSpinner));
		editor.add(Box.createVerticalStrut(5));

		final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(button.getHeight(), 16, 256, 1));
		heightSpinner.addChangeListener(e -> updateButton(button.getId(), b -> b.setHeight((int) heightSpinner.getValue()), false));
		editor.add(labeledRow("Height", heightSpinner));
		editor.add(Box.createVerticalStrut(menuPadding));

		// Resizable - Button Setting
		final JCheckBox resizableBox = new JCheckBox("Resizable (alt-drag edges)", button.isResizable());
		resizableBox.addActionListener(e -> updateButton(button.getId(), b -> b.setResizable(resizableBox.isSelected()), false));
		final JPanel resizableBoxRow = new JPanel(new BorderLayout());
		resizableBoxRow.add(resizableBox);
		editor.add(resizableBoxRow, BorderLayout.WEST);
		editor.add(Box.createVerticalStrut(menuPadding));

		// Shape - Button Setting
		final JComboBox<ToggleButtonsShape> shapeSelect = new JComboBox<>(ToggleButtonsShape.values());
		shapeSelect.setSelectedItem(button.getShape());
		shapeSelect.addActionListener(e -> updateButton(button.getId(),
			b -> b.setShape((ToggleButtonsShape) shapeSelect.getSelectedItem()), false));
		editor.add(labeledRow("Shape", shapeSelect));
		editor.add(Box.createVerticalStrut(menuPadding));

		// Toggle while held (peek) - Button Setting
		final JCheckBox heldBox = new JCheckBox("Toggle only while held (peek)", button.isToggleWhileHeld());
		heldBox.setToolTipText(ToggleButtonsStyle.tooltip("Plugins are toggled while the left mouse button is held on the button, then toggled back on release"));
		heldBox.addActionListener(e -> updateButton(button.getId(), b -> b.setToggleWhileHeld(heldBox.isSelected()), false));
		final JPanel heldBoxRow = new JPanel(new BorderLayout());
		heldBoxRow.add(heldBox);
		editor.add(heldBoxRow);
		editor.add(Box.createVerticalStrut(menuPadding));

		// Colors - Button Setting
		final JPanel colorRow = new JPanel(new GridLayout(1, 2, 4, 0));
		colorRow.add(colorButton("Color", new Color(button.getButtonColor(), true),
			c -> updateButton(button.getId(), b -> b.setButtonColor(c.getRGB()), false)));
		colorRow.add(colorButton("Pressed", new Color(button.getPressedColor(), true),
			c -> updateButton(button.getId(), b -> b.setPressedColor(c.getRGB()), false)));
		editor.add(colorRow);
		editor.add(Box.createVerticalStrut(10));

		// Plugins toggled - Button Setting
		final JLabel pluginToggleText = new JLabel(
			"Plugins toggled by this button");
		final JPanel pluginToggleLabelRow = new JPanel(new BorderLayout());
		pluginToggleLabelRow.add(pluginToggleText, BorderLayout.CENTER);
		editor.add(pluginToggleLabelRow);

		editor.add(Box.createVerticalStrut(5));

		final boolean atTargetLimit = button.getTargets().size() >= ToggleButtonsButton.MAX_TARGETS;

		final JComboBox<String> pluginSelect = new JComboBox<>();
		for (String pluginName : toggle.getTogglablePluginNames())
		{
			if (button.getTargets().stream().noneMatch(t -> pluginName.equalsIgnoreCase(t.getPluginName())))
			{
				pluginSelect.addItem(pluginName);
			}
		}
		pluginSelect.setEnabled(!atTargetLimit);

		final JButton addPluginButton = new JButton("Add");
		addPluginButton.setToolTipText(atTargetLimit
			? ToggleButtonsStyle.tooltip("This button has reached the maximum of " + ToggleButtonsButton.MAX_TARGETS + " plugins")
			: "Add plugin to button");
		addPluginButton.setEnabled(!atTargetLimit);
		addPluginButton.addActionListener(e ->
		{
			final String selected = (String) pluginSelect.getSelectedItem();
			if (selected != null && button.getTargets().size() < ToggleButtonsButton.MAX_TARGETS)
			{
				updateButton(button.getId(), b -> b.getTargets().add(new ToggleButtonsTarget(selected, true)));
			}
		});

		final JPanel addPluginRow = new JPanel(new BorderLayout(4, 0));
		addPluginRow.add(pluginSelect, BorderLayout.CENTER);
		addPluginRow.add(addPluginButton, BorderLayout.EAST);
		editor.add(addPluginRow);
		editor.add(Box.createVerticalStrut(menuPadding));

		for (ToggleButtonsTarget target : button.getTargets())
		{
			editor.add(buildTargetRow(button.getId(), target));
			editor.add(Box.createVerticalStrut(4));
		}

		editor.add(Box.createVerticalStrut(10));

		// Remove button
		final JButton removeButton = new JButton("Remove button");
		removeButton.setToolTipText(ToggleButtonsStyle.tooltip("Remove this button from the grid"));
		removeButton.setBackground(ToggleButtonsStyle.DESTRUCTIVE_BACKGROUND);
		removeButton.setForeground(ToggleButtonsStyle.TEXT_COLOR);
		removeButton.addActionListener(e ->
		{
			ToggleButtonsImageLoader.delete(button.getIconImagePath());
			final List<ToggleButtonsButton> buttons = store.getButtons();
			buttons.removeIf(b -> b.getId().equals(button.getId()));
			store.setButtons(buttons);
			selectedId = null;
			rebuild();
		});
		final JPanel removeButtonRow = new JPanel(new BorderLayout());
		removeButtonRow.add(removeButton, BorderLayout.WEST);
		editor.add(removeButtonRow);

		return editor;
	}

	private JPanel buildTargetRow(String buttonId, ToggleButtonsTarget target)
	{
		final JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBorder(new EmptyBorder(4, 4, 4, 4));
		row.setBackground(ToggleButtonsStyle.PANEL_ACCENT);

		final JLabel name = new JLabel(target.getPluginName());
		name.setToolTipText(target.getPluginName());
		row.add(name, BorderLayout.NORTH);

		final JComboBox<String> modeSelect = new JComboBox<>(new String[] { MODE_ENABLE_DISABLE, MODE_ON_OFF });
		modeSelect.setSelectedItem(target.isDisablePlugin() ? MODE_ENABLE_DISABLE : MODE_ON_OFF);
		modeSelect.setToolTipText(ToggleButtonsStyle.tooltip(
			"Enable/disable: the button enables or disables the plugin in RuneLite. Persistent in your config.<br>"
				+ "Turn on/off: the button only stops or starts the plugin without persisting across sessions."));
		modeSelect.addActionListener(e ->
		{
			final boolean disablePlugin = MODE_ENABLE_DISABLE.equals(modeSelect.getSelectedItem());
			updateButton(buttonId, b -> b.getTargets().stream()
				.filter(t -> target.getPluginName().equalsIgnoreCase(t.getPluginName()))
				.forEach(t -> t.setDisablePlugin(disablePlugin)), false);
		});
		row.add(modeSelect, BorderLayout.CENTER);

		final JButton removeTargetButton = new JButton("X");
		removeTargetButton.setToolTipText("Remove plugin from button");
		removeTargetButton.addActionListener(e -> updateButton(buttonId, b -> b.getTargets().removeIf(
			t -> target.getPluginName().equalsIgnoreCase(t.getPluginName()))));
		row.add(removeTargetButton, BorderLayout.EAST);

		return row;
	}

	private JPanel labeledRow(String label, java.awt.Component component)
	{
		final JPanel row = new JPanel(new BorderLayout(4, 0));
		final JLabel jLabel = new JLabel(label);
		jLabel.setPreferredSize(new Dimension(50, jLabel.getPreferredSize().height));
		row.add(jLabel, BorderLayout.WEST);
		row.add(component, BorderLayout.CENTER);
		return row;
	}

	private JButton colorButton(String label, Color current, java.util.function.Consumer<Color> onChange)
	{
		final JButton button = new JButton(label);
		button.setBackground(current);
		button.addActionListener(e ->
		{
			final RuneliteColorPicker picker = colorPickerManager.create(
				SwingUtilities.windowForComponent(this), current, label, false);
			picker.setOnColorChange(c ->
			{
				button.setBackground(c);
				onChange.accept(c);
			});
			picker.setLocationRelativeTo(button);
			picker.setVisible(true);
		});
		return button;
	}

	private void saveName(String buttonId, String name)
	{
		final String trimmed = name.trim();
		if (trimmed.isEmpty())
		{
			return;
		}
		updateButton(buttonId, b -> b.setName(trimmed), false);
	}

	private void updateButton(String buttonId, java.util.function.Consumer<ToggleButtonsButton> mutation)
	{
		updateButton(buttonId, mutation, true);
	}

	private void updateButton(String buttonId, java.util.function.Consumer<ToggleButtonsButton> mutation, boolean rebuildAfter)
	{
		final List<ToggleButtonsButton> buttons = store.getButtons();
		for (ToggleButtonsButton button : buttons)
		{
			if (button.getId().equals(buttonId))
			{
				mutation.accept(button);
			}
		}
		store.setButtons(buttons);
		if (rebuildAfter)
		{
			rebuild();
		}
	}
}
