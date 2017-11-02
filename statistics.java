import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Objects;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Frame extends JFrame {
	final int MAX_ARRAY_SIZE = 1000;
	final int TABLE_COLUMN_LENGTH = 4;

	public int dataLen = 0, statisticsLen = 0, t = 12, i, j;

	public String
		statistics[][] = new String[MAX_ARRAY_SIZE][3];

	private String
		columnName[] = new String[TABLE_COLUMN_LENGTH],
		data[][] = new String[MAX_ARRAY_SIZE][TABLE_COLUMN_LENGTH];

	public Font
		ft0 = new Font("方正卡通简体", Font.PLAIN, 67),
		ft1 = new Font("方正卡通简体", Font.PLAIN, 21),
		ft2 = new Font("微软雅黑", Font.PLAIN, 13);

	public JTable table;
	public JButton importBtn, exportBtn;

	public Frame() {
		setSize(900,600);
		setLocation(50,50);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		importPrepare();
	}

	public void importPrepare() {
		importBtn = new JButton("导入统计数据");
		importBtn.setFont(ft0);
		importBtn.setFocusPainted(false);

		add(importBtn, BorderLayout.CENTER);

		importBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importBtn.setVisible(false);
				readData();
				drawInterface();
			}
		});
	}

	public void readData() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("data.csv"));

			String line = null;

			line = reader.readLine();
			columnName = line.split(",");

			while((line = reader.readLine()) != null) {
				String it[] = line.split(",");

				data[dataLen][0] = it[0];
				data[dataLen][1] = it[1];
				data[dataLen][2] = it[2];
				data[dataLen][3] = it[3];

				dataLen++;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawInterface() {
		table = new JTable(new AbstractTableModel() {
			public String getColumnName(int column) {
				return columnName[column];
			}

			public int getColumnCount() {
				return columnName.length;
			}

			public int getRowCount() {
				return dataLen;
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
		});

		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
		tcr.setHorizontalAlignment(SwingConstants.CENTER);
		table.setDefaultRenderer(Object.class, tcr);

		table.setAutoCreateRowSorter(true);
		table.setFont(ft2);
		table.setRowHeight(21);

		JScrollPane scrollpane = new JScrollPane(table);
		add(scrollpane, BorderLayout.CENTER);

		exportBtn = new JButton("导出统计数据");
		exportBtn.setFont(ft1);
		exportBtn.setFocusPainted(false);

		add(exportBtn, BorderLayout.NORTH);

		exportBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statistics();
				outputData();
			}
		});
	}

	public void statistics() {
		for (i = 0; i < dataLen; i++) {
			j = 0;
			while(j < statisticsLen
				&& !Objects.equals(Character.toString(data[i][1].charAt(0)), statistics[j][0]))
				j++;

			int id = Objects.equals(data[i][2], "男") ? 1 : 2;
			if (j < statisticsLen) {
				statistics[j][id] = String.valueOf(Integer.parseInt(statistics[j][id]) + 1);
			} else {
				statistics[statisticsLen][0] = Character.toString(data[i][1].charAt(0));
				statistics[statisticsLen][id] = "1";
				statistics[statisticsLen][3 - id] = "0";

				statisticsLen++;
			}
		}
	}

	public void outputData() {
		String
			exportFile = "statistics.csv",
			statisticsHeader = "数据统计结果",
			statisticsMessage =
			"<html>" +
			"<p>总人数： " + dataLen + "</p>" + "<br><hr>" +
			"<table style='width:100%;'>" + "<tbody>";
		try {
			FileWriter writer = new FileWriter(exportFile);
			writer.append("姓氏,总人数,男生,女生\n");
			for (i = 0; i < statisticsLen; i++)
				writer.append(
					statistics[i][0] + ',' +
					String.valueOf(Integer.parseInt(statistics[i][1]) +
						Integer.parseInt(statistics[i][2])) + ',' +
					statistics[i][1] + ',' +
					statistics[i][2] + '\n'
				);
			writer.flush();
			writer.close();

			statisticsMessage += "<tr><td><strong>姓氏</strong></td>";
			for (int i = 0; i < statisticsLen; i++)
				statisticsMessage += "<td>" + statistics[i][0] + "</td>";
			statisticsMessage += "</tr>";

			statisticsMessage += "<tr><td><strong>总人数</strong></td>";
			for (int i = 0; i < statisticsLen; i++)
				statisticsMessage +=
				"<td>" +
					String.format("%3s", Integer.parseInt(statistics[i][1])
						+ Integer.parseInt(statistics[i][2])) +
				"</td>";
			statisticsMessage += "</tr>";

			statisticsMessage += "<tr><td><strong>男生</strong></td>";
			for (int i = 0; i < statisticsLen; i++)
				statisticsMessage += "<td>" + String.format("%3s", statistics[i][1]) + "</td>";
			statisticsMessage += "</tr>";

			statisticsMessage += "<tr><td><strong>女生</strong></td>";
			for (int i = 0; i < statisticsLen; i++)
				statisticsMessage += "<td>" + String.format("%3s", statistics[i][2]) + "</td>";
			statisticsMessage += "</tr>";

			statisticsMessage +=
			"</tbody>" + "</table>" + "<hr>" + "<br>" +
			"<p style='text-align: center;'>统计数据已导出至 " + exportFile +
			"&nbsp;&nbsp;&nbsp;点击 【确定】 后将显示具体信息并打开文件</p>" + "<br>" + "</html>";

			UIManager.put("OptionPane.messageFont", ft2);

			int choice = JOptionPane.showConfirmDialog(null,
				statisticsMessage,
				statisticsHeader,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
			);

			if (choice == JOptionPane.OK_OPTION) {
				String infoHeader = "具体统计信息", infoMessage = "<html>";
				for (int i = 0; i < statisticsLen; i++) {
					infoMessage +=
					"<div>" +
						"&nbsp;&nbsp;&nbsp;姓氏：" + statistics[i][0] +
						"&nbsp;&nbsp;&nbsp;总人数：" + String.valueOf(Integer.parseInt(statistics[i][1]) +
							Integer.parseInt(statistics[i][2])) +
						"&nbsp;&nbsp;&nbsp;男生：" + statistics[i][1] +
						"&nbsp;&nbsp;&nbsp;女生：" + statistics[i][2] +
						"<hr>" +
						"<table style='width:100%;'>" +
							"<thead>" +
								"<tr>" +
									"<td> 学号 </td>" +
									"<td> 姓名 </td>" +
									"<td> 性别 </td>" +
									"<td> 籍贯 </td>" +
								"</tr>" +
							"</thead>" +
							"<tbody>";

					for (int j = 0; j < dataLen; j++) {
						if (Objects.equals(Character.toString(data[j][1].charAt(0)), statistics[i][0])) {
							infoMessage +=
							"<tr>" +
								"<td>" + data[j][0] + "</td>" +
								"<td>" + data[j][1] + "</td>" +
								"<td>" + data[j][2] + "</td>" +
								"<td>" + data[j][3] + "</td>" +
							"</tr>";
						}
					}
					infoMessage += "</tbody>" + "</table>" + "</div>" + "<br><br><br>";
				}
				infoMessage += "</html>";
				JOptionPane.showMessageDialog(null, infoMessage, infoHeader, JOptionPane.PLAIN_MESSAGE);
				// Process process = Runtime.getRuntime().exec("cmd /c statistics.csv");
			} else {
				Process process = Runtime.getRuntime().exec("java statistics");
				System.exit(0);
			}
		} catch(IOException err) {
			err.printStackTrace();
		}
	}
}

public class statistics {
	public static void main(String[] args) {
		Frame frame = new Frame();
		frame.setVisible(true);
	}
}