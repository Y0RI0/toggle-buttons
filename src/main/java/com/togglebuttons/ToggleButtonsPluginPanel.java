package com.togglebuttons;

/*
* Crafts the view of the plugin's sidebar panel,
* where buttons are managed in a grid and the
* selected button's settings are edited below it
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
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

	private final JPanel buttonGrid = new JPanel(new GridLayout(0, GRID_COLUMNS, 4, 4));
	private final JPanel editorContainer = new JPanel();

	private String selectedId;

	@Inject
	ToggleButtonsPluginPanel(
		ToggleButtonsToggle toggle,
		ToggleButtonsButtonStore store,
		ToggleButtonsIconSearch iconSearch,
		ItemManager itemManager,
		ColorPickerManager colorPickerManager)
	{
		this.toggle = toggle;
		this.store = store;
		this.iconSearch = iconSearch;
		this.itemManager = itemManager;
		this.colorPickerManager = colorPickerManager;

		setLayout(new BorderLayout(0, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		final JButton addButton = new JButton("Add new button");
		addButton.setToolTipText("Add a new button to the grid");
		addButton.addActionListener(e ->
		{
			selectedId = store.createButton().getId();
			rebuild();
		});

		final JPanel header = new JPanel(new BorderLayout(0, 5));
		header.add(new JLabel("Buttons"), BorderLayout.NORTH);
		header.add(addButton, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		final JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		final JPanel gridWrapper = new JPanel(new BorderLayout());
		gridWrapper.add(buttonGrid, BorderLayout.NORTH);
		content.add(gridWrapper);
		content.add(Box.createVerticalStrut(10));
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
		final JButton cell = new JButton();
		cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
		cell.setToolTipText(button.getName());
		cell.setFocusPainted(false);

		if (button.getIconItemId() >= 0)
		{
			final AsyncBufferedImage img = itemManager.getImage(button.getIconItemId());
			img.addTo(cell);
		}

		if (button.getId().equals(selectedId))
		{
			cell.setBorder(BorderFactory.createLineBorder(ColorScheme.BRAND_ORANGE, 2));
		}

		cell.addActionListener(e ->
		{
			selectedId = button.getId();
			rebuild();
		});

		return cell;
	}

	private JPanel buildEditor(ToggleButtonsButton button)
	{
		final JPanel editor = new JPanel();
		editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
		editor.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR),
			new EmptyBorder(6, 6, 6, 6)));

		// Name
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
		editor.add(Box.createVerticalStrut(5));

		// Icon
		final JButton selectIconButton = new JButton("Select icon");
		selectIconButton.setToolTipText("Search for an item to use as the button icon (requires being logged in)");
		selectIconButton.addActionListener(e -> iconSearch.open(itemId ->
			SwingUtilities.invokeLater(() -> updateButton(button.getId(), b -> b.setIconItemId(itemId)))));

		final JButton clearIconButton = new JButton("Clear icon");
		clearIconButton.addActionListener(e -> updateButton(button.getId(), b -> b.setIconItemId(-1)));

		final JPanel iconRow = new JPanel(new GridLayout(1, 2, 4, 0));
		iconRow.add(selectIconButton);
		iconRow.add(clearIconButton);
		editor.add(iconRow);
		editor.add(Box.createVerticalStrut(5));

		// Size
		final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(button.getWidth(), 16, 256, 1));
		widthSpinner.addChangeListener(e -> updateButton(button.getId(), b -> b.setWidth((int) widthSpinner.getValue()), false));
		editor.add(labeledRow("Width", widthSpinner));
		editor.add(Box.createVerticalStrut(5));

		final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(button.getHeight(), 16, 256, 1));
		heightSpinner.addChangeListener(e -> updateButton(button.getId(), b -> b.setHeight((int) heightSpinner.getValue()), false));
		editor.add(labeledRow("Height", heightSpinner));
		editor.add(Box.createVerticalStrut(5));

		// Resizable
		final JCheckBox resizableBox = new JCheckBox("Resizable (alt-drag edges)", button.isResizable());
		resizableBox.addActionListener(e -> updateButton(button.getId(), b -> b.setResizable(resizableBox.isSelected()), false));
		editor.add(resizableBox);
		editor.add(Box.createVerticalStrut(5));

		// Colors
		final JPanel colorRow = new JPanel(new GridLayout(1, 2, 4, 0));
		colorRow.add(colorButton("Color", new Color(button.getButtonColor(), true),
			c -> updateButton(button.getId(), b -> b.setButtonColor(c.getRGB()), false)));
		colorRow.add(colorButton("Pressed", new Color(button.getPressedColor(), true),
			c -> updateButton(button.getId(), b -> b.setPressedColor(c.getRGB()), false)));
		editor.add(colorRow);
		editor.add(Box.createVerticalStrut(10));

		// Plugins toggled by this button
		editor.add(new JLabel("Plugins toggled by this button"));
		editor.add(Box.createVerticalStrut(5));

		final JComboBox<String> pluginSelect = new JComboBox<>();
		for (String pluginName : toggle.getTogglablePluginNames())
		{
			if (button.getTargets().stream().noneMatch(t -> pluginName.equalsIgnoreCase(t.getPluginName())))
			{
				pluginSelect.addItem(pluginName);
			}
		}

		final JButton addPluginButton = new JButton("Add");
		addPluginButton.setToolTipText("Add plugin to button");
		addPluginButton.addActionListener(e ->
		{
			final String selected = (String) pluginSelect.getSelectedItem();
			if (selected != null)
			{
				updateButton(button.getId(), b -> b.getTargets().add(new ToggleButtonsTarget(selected, true)));
			}
		});

		final JPanel addPluginRow = new JPanel(new BorderLayout(4, 0));
		addPluginRow.add(pluginSelect, BorderLayout.CENTER);
		addPluginRow.add(addPluginButton, BorderLayout.EAST);
		editor.add(addPluginRow);
		editor.add(Box.createVerticalStrut(5));

		for (ToggleButtonsTarget target : button.getTargets())
		{
			editor.add(buildTargetRow(button.getId(), target));
			editor.add(Box.createVerticalStrut(4));
		}

		editor.add(Box.createVerticalStrut(10));

		// Remove button
		final JButton removeButton = new JButton("Remove button");
		removeButton.setToolTipText("Remove this button from the grid");
		removeButton.addActionListener(e ->
		{
			final List<ToggleButtonsButton> buttons = store.getButtons();
			buttons.removeIf(b -> b.getId().equals(button.getId()));
			store.setButtons(buttons);
			selectedId = null;
			rebuild();
		});
		editor.add(removeButton);

		return editor;
	}

	private JPanel buildTargetRow(String buttonId, ToggleButtonsTarget target)
	{
		final JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBorder(new EmptyBorder(4, 4, 4, 4));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		final JLabel name = new JLabel(target.getPluginName());
		name.setToolTipText(target.getPluginName());
		row.add(name, BorderLayout.NORTH);

		final JComboBox<String> modeSelect = new JComboBox<>(new String[]{MODE_ENABLE_DISABLE, MODE_ON_OFF});
		modeSelect.setSelectedItem(target.isDisablePlugin() ? MODE_ENABLE_DISABLE : MODE_ON_OFF);
		modeSelect.setToolTipText("Enable/disable: the button enables or disables the plugin in RuneLite. "
			+ "Turn on/off: the button only stops or starts the plugin without changing its enabled setting.");
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
		removeTargetButton.addActionListener(e ->
			updateButton(buttonId, b -> b.getTargets().removeIf(
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
