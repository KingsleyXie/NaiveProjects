import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.Scanner;

class Global {
	public static final String
		CLOSE_FLAG = "Bye",
		SYSTEM_MSG = "SYS:",
		USER_MSG = "USR:";
	public static final int
		DEFAULT_PORT = 2333,
		START_ID = 1000,
		PREFIX_LEN = USER_MSG.length() + 4;

	public static int port = DEFAULT_PORT;
	public static ServerSocket server;
}

class listen implements Runnable {
	private chatFrame frm;
	private Socket socket;
	private BufferedReader in;
	private String name;
	private int id;
	private boolean isServer = true, running = true;

	public listen(
		chatFrame frm, Socket socket,
		BufferedReader in,
		int id, String name
	) {
		this.frm = frm; this.socket = socket;
		this.in = in; this.id = id; this.name = name;
		if (id == 0) isServer = false;
		new Thread(this).start();
	}

	public void run() {
		try {
			while (running) {
				String str = in.readLine();
				if (isServer) {
					if (str.equals(Global.CLOSE_FLAG)) {
						server.broadcast(name + " 退出了聊天室", true);
						socket.close(); multiServer.count--;
						break;
					}
					server.broadcast(Global.USER_MSG + id + name + ": " + str);
				} else {
					switch (str.substring(0, Math.min(str.length(), 4))) {
						case Global.SYSTEM_MSG:
							frm.append(
								str.replace(Global.SYSTEM_MSG, ""),
								chatFrame.msgType.SYSTEM
							);
							break;

						case Global.USER_MSG:
							frm.append(
								str.substring(Global.PREFIX_LEN),
								chatFrame.msgType.INCOME
							);
							break;
					}
				}
			}
		} catch (SocketException e) {
			//L46: Always waiting for new incoming messages
		} catch (Exception e) { e.printStackTrace(); }
	}

	public void stop() {
		running = false;
	}
}

class multiServer extends Thread {
	public static int count = Global.START_ID;
	private String name;
	private int id;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	public multiServer(Socket s) throws IOException {
		socket = s; id = count++;
		in = new BufferedReader(
			new InputStreamReader(socket.getInputStream())
		);

		out = new PrintWriter(new BufferedWriter(
			new OutputStreamWriter(socket.getOutputStream())
		), true);

		server.frame.btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String s = server.frame.textArea.getText();
				switch (s.substring(0, Math.min(s.length(), 4))) {
					case Global.SYSTEM_MSG:
						out.println(s);
						break;

					case Global.USER_MSG:
						if (s.indexOf(
							Global.USER_MSG + String.valueOf(id)
						) != 0) out.println(s);
						break;

					default:
						out.println(Global.SYSTEM_MSG + s);
						break;
				}
			}
		});
	}

	public void run() {
		try {
			name = in.readLine();
			server.broadcast(name + " 加入了聊天室", true);

			new listen(server.frame, socket, in, id, name);
		} catch (Exception e) { e.printStackTrace(); }
	}
}

class server {
	public static chatFrame frame;
	server() throws Exception {
		Global.server = new ServerSocket(Global.port);
		frame = new chatFrame("Socket Server", true);
		frame.append("服务端正在运行中，端口号：" + Global.port, chatFrame.msgType.SYSTEM);

		frame.btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String s = server.frame.textArea.getText();
				switch (s.substring(0, Math.min(s.length(), 4))) {
					case Global.SYSTEM_MSG:
						server.frame.append(
							s.replace(Global.SYSTEM_MSG, ""),
							chatFrame.msgType.SYSTEM
						);
						break;

					case Global.USER_MSG:
						server.frame.append(
							s.substring(Global.PREFIX_LEN),
							chatFrame.msgType.INCOME
						);
						break;

					default:
						server.frame.append(
							s, chatFrame.msgType.OUTCOME
						);
						break;
				}
			}
		});

		frame.textArea.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				frame.textArea.setText("");
			}
			public void mousePressed(MouseEvent e) {};
			public void mouseReleased(MouseEvent e) {};
			public void mouseEntered(MouseEvent e) {};
			public void mouseExited(MouseEvent e) {};
		});

		while (true) {
			Socket socket = Global.server.accept();
			new multiServer(socket).start();
		}
	}

	public static void broadcast(String s) {
		broadcast(s, false);
	}

	public static void broadcast(String s, boolean sys) {
		frame.textArea.setText((sys ? Global.SYSTEM_MSG : "") + s);
		frame.btn.doClick();
	}
}

class client {
	public static int clientN = 1;
	private chatFrame frame;
	private String name;

	client() throws Exception {
		name = JOptionPane.showInputDialog(null,
			"请输入你的用户名﻿",
			"进入聊天室",
			JOptionPane.PLAIN_MESSAGE
		);
		if (name == null) System.exit(0);

		InetAddress addr = InetAddress.getByName(null);
		Socket socket = new Socket(addr, Global.port);

		BufferedReader in = new BufferedReader(
			new InputStreamReader(socket.getInputStream())
		);

		PrintWriter out = new PrintWriter(new BufferedWriter(
			new OutputStreamWriter(socket.getOutputStream())
		), true);

		frame = new chatFrame(name, false);
		listen lsn = new listen(frame, socket, in, 0, name);
		out.println(name);

		frame.btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextArea t = frame.textArea;
				String msg = t.getText();
				out.println(msg);
				t.setText("");

				if (msg.equals(Global.CLOSE_FLAG)) {
					lsn.stop();
					t.setEditable(false);
					frame.btn.setEnabled(false);
				}
				else
					frame.append(msg, chatFrame.msgType.OUTCOME);
			}
		});
	}
}

public class socket {
	private static void showInfo() {
		System.out.println(
			"Usage:\n" +
			"\tjava socket -s|--server [-p|--port p]\n" +
			"\t(Run as socket server at port 2333 or p)\n" +
			"\n" +
			"\tjava socket -c|--client [-n|--number n] [-p|--port p]\n" +
			"\t(Run 1 or n socket client at port 2333 or p)"
		);
	}

	public static void customizePort(String p) {
		try {
			Global.port = Integer.parseInt(p);
		} catch (NumberFormatException e) {
			System.out.println("\tPlease Input A Correct Port Number!");
			System.exit(0);
		}
	}

	public static void multiClients(String n) {
		try {
			int num = Integer.parseInt(n);
			client.clientN = num;
			for (int i = 0; i < num; i++)
				shunt("-c");
		} catch (NumberFormatException e) {
			System.out.println("\tPlease Input A Correct Client Number!");
			System.exit(0);
		}
	}

	public static void shunt(String cmd) {
		try {
			switch (cmd) {
				case "-s":
				case "--server":
					new server();
					break;

				case "-c":
				case "--client":
					new client();
					break;

				default:
					showInfo();
					break;
			}
		} catch (ConnectException e) {
			System.out.println(
				"\n\tError: Socket server not running at port " +
				Global.port
			);
			System.exit(0);
		} catch (BindException e) {
			System.out.println(
				"\n\tError: Port " + Global.port + " already in use"
			);
		} catch (SocketException e) {
			//L176: Always waiting for new connection
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		switch (args.length) {
			case 1:
				shunt(args[0]);
				break;

			case 3:
				switch (args[1]) {
					case "-p":
					case "--port":
						customizePort(args[2]);
						shunt(args[0]);
						break;

					case "-n":
					case "--number":
						if (!(args[0].equals("--client")
							|| args[0].equals("-c")))
							showInfo();
						else
							multiClients(args[2]);
						break;

					default:
						showInfo();
						break;
				}
				break;

			case 5:
				if (!((args[0].equals("--client")
						|| args[0].equals("-c"))
					&& (args[1].equals("--number")
						|| args[1].equals("-n"))
					&& (args[3].equals("--port")
						|| args[3].equals("-p"))
				)) {
					showInfo();
					break;
				}
				customizePort(args[4]);
				multiClients(args[2]);
				break;

			default:
				showInfo();
				break;
		}
	}
}



class chatFrame extends JFrame {
	public Container pane;
	public JTextArea textArea;
	public JButton btn;

	public static enum msgType {
		SYSTEM, INCOME, OUTCOME
	};

	chatFrame(String tit, boolean isServer) {
		setTitle(tit);
		setLocation(50, 10);
		setLayout(new BorderLayout());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				if (isServer) {
					try {
						Global.server.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.exit(0);
				} else {
					textArea.setText(Global.CLOSE_FLAG);
					btn.doClick();

					client.clientN--;
					if (client.clientN == 0)
						System.exit(0);
				}
			}
		});

		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		JScrollPane chatContent = new JScrollPane(pane);
		chatContent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(chatContent, BorderLayout.CENTER);

		textArea = new JTextArea(2, 50);
		btn = new JButton("发送");
		btn.setBackground(new Color(3, 155, 229));
		btn.setForeground(Color.WHITE);

		JPanel bottom = new JPanel();
		bottom.add(new JScrollPane(textArea));
		bottom.add(btn);

		add(bottom, BorderLayout.SOUTH);
		setVisible(true);
	}

	public void append(String text, msgType mt) {
		JTextPane output = new JTextPane();
		output.setEditable(false);

		SimpleAttributeSet attribs = new SimpleAttributeSet();
		switch (mt) {
			case SYSTEM:
				text = "【系统消息】 " + text;
				output.setBorder(new TextBubbleBorder(
					new Color(66, 66, 66), 2, 10, 6, false, this)
				);
				StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
				break;

			case INCOME:
				output.setBorder(new TextBubbleBorder(
					new Color(255, 145, 0), 2, 10, 6, false, this)
				);
				StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_LEFT);
				break;

			case OUTCOME:
				output.setBorder(new TextBubbleBorder(
					new Color(0, 176, 255), 2, 10, 6, true, this)
				);
				StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_RIGHT);
				break;
		}

		output.setText(text);
		output.setParagraphAttributes(attribs, false);
		pane.add(output);

		JTextPane gap = new JTextPane();
		gap.setPreferredSize(
			new Dimension(
				getWidth() < 500 ? getWidth() : 500, 9
			)
		);
		pane.add(gap);

		pack();
		setSize(
			getWidth() > 500 ? getWidth() : 500,
			getHeight() < 700 ? getHeight() : 700
		);
	}
}





// Class Modified From:
//   https://stackoverflow.com/questions/15025092/border-with-rounded-corners-transparency
class TextBubbleBorder extends AbstractBorder {
	private Color color;
	private Insets insets = null;
	private BasicStroke stroke = null;
	private int thickness, radii, pointerSize, strokePad, pointerPad;
	private boolean rightPointer;
	private chatFrame frame;
	RenderingHints hints;

	TextBubbleBorder(
		Color color,
		int thickness, int radii, int pointerSize,
		boolean rightPointer, chatFrame frame
	) {
		this.color = color; this.thickness = thickness;
		this.radii = radii; this.pointerSize = pointerSize;
		this.rightPointer = rightPointer; this.frame = frame;

		stroke = new BasicStroke(thickness);
		strokePad = thickness / 2;

		hints = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		);

		int pad = radii + strokePad;
		int bottomPad = pad + pointerSize + strokePad;
		insets = new Insets(pad, pad, bottomPad, pad);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		return getBorderInsets(c);
	}

	@Override
	public void paintBorder(
			Component c,
			Graphics g,
			int x, int y,
			int width, int height) {

		Graphics2D g2 = (Graphics2D) g;

		int bottomLineY = height - thickness - pointerSize;

		RoundRectangle2D.Double bubble =
			new RoundRectangle2D.Double(
				0 + strokePad,
				0 + strokePad,
				width - thickness,
				bottomLineY,
				radii,
				radii
			);

		Polygon pointer = new Polygon();
		if (this.rightPointer) {
			int offset =
				frame.getWidth() -
				2 * ((Integer)UIManager.get("ScrollBar.width")).intValue();

			pointer.addPoint(
					offset - (strokePad + radii + pointerPad),
					bottomLineY);

			pointer.addPoint(
					offset - (strokePad + radii + pointerPad + pointerSize),
					bottomLineY);

			pointer.addPoint(
					offset - (strokePad + radii + pointerPad + (pointerSize / 2)),
					height - strokePad);
		} else {
			pointer.addPoint(
					strokePad + radii + pointerPad,
					bottomLineY);

			pointer.addPoint(
					strokePad + radii + pointerPad + pointerSize,
					bottomLineY);

			pointer.addPoint(
					strokePad + radii + pointerPad + (pointerSize / 2),
					height - strokePad);
		}

		Area area = new Area(bubble);
		area.add(new Area(pointer));

		g2.setRenderingHints(hints);

		Area spareSpace = new Area(new Rectangle(0, 0, width, height));
		spareSpace.subtract(area);
		g2.setClip(spareSpace);
		g2.clearRect(0, 0, width, height);
		g2.setClip(null);

		g2.setColor(color);
		g2.setStroke(stroke);
		g2.draw(area);
	}
}
