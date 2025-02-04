package gg.xp.xivsupport.events.triggers.jobs.gui;

import gg.xp.xivsupport.gui.tables.CustomColumn;
import gg.xp.xivsupport.gui.tables.CustomTableModel;
import gg.xp.xivsupport.gui.tables.renderers.ActionAndStatusRenderer;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BaseCdTrackerTable {
	private final CustomTableModel<VisualCdInfo> tableModel;
	private final JTable table;
	private static final int BAR_WIDTH = 150;
	public BaseCdTrackerTable(Supplier<List<? extends VisualCdInfo>> supplier) {
		tableModel = CustomTableModel.builder(supplier)
				.addColumn(new CustomColumn<>("Icon", c -> c.getEvent().getAbility(), c -> {
					c.setCellRenderer(new ActionAndStatusRenderer(true, false, false));
					c.setMaxWidth(22);
					c.setMinWidth(22);
				}))
				.addColumn(new CustomColumn<>("Bar", Function.identity(),
						c -> {
							c.setCellRenderer(new CdBarRenderer());
							c.setMaxWidth(BAR_WIDTH);
							c.setMinWidth(BAR_WIDTH);
						}))
				.build();
		table = new JTable(tableModel);
		table.setOpaque(false);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		tableModel.configureColumns(table);
	}

	public CustomTableModel<VisualCdInfo> getTableModel() {
		return tableModel;
	}

	public JTable getTable() {
		return table;
	}
}
