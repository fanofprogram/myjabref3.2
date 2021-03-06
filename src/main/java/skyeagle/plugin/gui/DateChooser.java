package skyeagle.plugin.gui;

/*
 * DateChooser.java
 *
 * Created on 2007��8��22��, ����6:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author hadeslee
 */
public class DateChooser extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final Date initDate;
	private final Calendar now = Calendar.getInstance();
	private final Calendar select;
    private JPanel monthPanel;// ����
    private JP1 jp1;// �Ŀ����,���
	private JP2 jp2;
	private JP3 jp3;
	private JP4 jp4;
    private final Font font = new Font("����", Font.PLAIN, 12);
	private final LabelManager lm = new LabelManager();
	private JLabel showDate;// ,toSelect;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
	private boolean isShow = false;
	private Popup pop;

	/**
	 * Creates a new instance of DateChooser
	 */
	public DateChooser() {
		this(new Date());
	}

	public DateChooser(Date date) {
		initDate = date;
		select = Calendar.getInstance();
		select.setTime(initDate);
		initPanel();
		initLabel();
	}

	@Override
    public void setEnabled(boolean b) {
		super.setEnabled(b);
		showDate.setEnabled(b);
	}

	/**
     * �õ���ǰѡ��������
     */
	public Date getDate() {
		return select.getTime();
	}

    // ���ݳ�ʼ��������,��ʼ�����
	private void initPanel() {
		monthPanel = new JPanel(new BorderLayout());
		monthPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		JPanel up = new JPanel(new BorderLayout());
		up.add(jp1 = new JP1(), BorderLayout.NORTH);
		up.add(jp2 = new JP2(), BorderLayout.CENTER);
		monthPanel.add(jp3 = new JP3(), BorderLayout.CENTER);
		monthPanel.add(up, BorderLayout.NORTH);
		monthPanel.add(jp4 = new JP4(), BorderLayout.SOUTH);
		this.addAncestorListener(new AncestorListener() {
			@Override
            public void ancestorAdded(AncestorEvent event) {

			}

			@Override
            public void ancestorRemoved(AncestorEvent event) {

			}

            // ֻҪ�������һ�ƶ�,���Ͼ���popup��ʧ
			@Override
            public void ancestorMoved(AncestorEvent event) {
				hidePanel();
			}

		});
	}

    // ��ʼ����ǩ
	private void initLabel() {
		showDate = new JLabel(sdf.format(initDate));
		showDate.setRequestFocusEnabled(true);
		showDate.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
				showDate.requestFocusInWindow();
			}
		});
		// toSelect=new JLabel(sdf.format(initDate));
		// toSelect.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// toSelect.setRequestFocusEnabled(true);
		this.setBackground(Color.WHITE);
		this.add(showDate, BorderLayout.CENTER);
		// this.add(toSelect,BorderLayout.EAST);
		this.setPreferredSize(new Dimension(90, 25));
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		showDate.addMouseListener(new MouseAdapter() {
			@Override
            public void mouseEntered(MouseEvent me) {
				if (showDate.isEnabled()) {
					showDate.setCursor(new Cursor(Cursor.HAND_CURSOR));
					showDate.setForeground(Color.RED);
				}
			}

			@Override
            public void mouseExited(MouseEvent me) {
				if (showDate.isEnabled()) {
					showDate.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					showDate.setForeground(Color.BLACK);
				}
			}

			@Override
            public void mousePressed(MouseEvent me) {
				if (showDate.isEnabled()) {
					showDate.setForeground(Color.CYAN);
					if (isShow) {
						hidePanel();
					} else {
						showPanel(showDate);
					}
				}
			}

			@Override
            public void mouseReleased(MouseEvent me) {
				if (showDate.isEnabled()) {
					showDate.setForeground(Color.BLACK);
				}
			}
		});
		showDate.addFocusListener(new FocusListener() {
			@Override
            public void focusLost(FocusEvent e) {
				hidePanel();
			}

			@Override
            public void focusGained(FocusEvent e) {

			}
		});
	}

    // �����µ�����ˢ��
	private void refresh() {
		jp1.updateDate();
		jp3.updateDate();
		SwingUtilities.updateComponentTreeUI(this);
	}

    // �ύ����
	private void commit() {
        // System.out.println("ѡ�е������ǣ�"+sdf.format(select.getTime()));
		showDate.setText(sdf.format(select.getTime()));
		hidePanel();
	}

	private void hidePanel() {
		if (pop != null) {
			isShow = false;
			pop.hide();
			pop = null;
		}
	}

	private void showPanel(Component owner) {
		if (pop != null) {
			pop.hide();
		}
		Point show = new Point(0, showDate.getHeight());
		SwingUtilities.convertPointToScreen(show, showDate);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int x = show.x;
		int y = show.y;
		if (x < 0) {
			x = 0;
		}
		if (x > (size.width - 295)) {
			x = size.width - 295;
		}
		if (y < (size.height - 170)) {
		} else {
			y -= 188;
		}
		pop = PopupFactory.getSharedInstance().getPopup(owner, monthPanel, x, y);
		pop.show();
		isShow = true;
	}

	private class JP1 extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		JLabel left, right, center;

		public JP1() {
			super(new BorderLayout());
			this.setBackground(new Color(160, 185, 215));
			initJP1();
		}

		private void initJP1() {
			left = new JLabel(" << ", JLabel.CENTER);
            left.setToolTipText("上一月");
			right = new JLabel(" >> ", JLabel.CENTER);
            right.setToolTipText("下一月");
			left.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			right.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			center = new JLabel("", JLabel.CENTER);
			updateDate();
			this.add(left, BorderLayout.WEST);
			this.add(center, BorderLayout.CENTER);
			this.add(right, BorderLayout.EAST);
			this.setPreferredSize(new Dimension(295, 25));
			left.addMouseListener(new MouseAdapter() {
				@Override
                public void mouseEntered(MouseEvent me) {
					left.setCursor(new Cursor(Cursor.HAND_CURSOR));
					left.setForeground(Color.RED);
				}

				@Override
                public void mouseExited(MouseEvent me) {
					left.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					left.setForeground(Color.BLACK);
				}

				@Override
                public void mousePressed(MouseEvent me) {
					select.add(Calendar.MONTH, -1);
					left.setForeground(Color.WHITE);
					refresh();
				}

				@Override
                public void mouseReleased(MouseEvent me) {
					left.setForeground(Color.BLACK);
				}
			});
			right.addMouseListener(new MouseAdapter() {
				@Override
                public void mouseEntered(MouseEvent me) {
					right.setCursor(new Cursor(Cursor.HAND_CURSOR));
					right.setForeground(Color.RED);
				}

				@Override
                public void mouseExited(MouseEvent me) {
					right.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					right.setForeground(Color.BLACK);
				}

				@Override
                public void mousePressed(MouseEvent me) {
					select.add(Calendar.MONTH, 1);
					right.setForeground(Color.WHITE);
					refresh();
				}

				@Override
                public void mouseReleased(MouseEvent me) {
					right.setForeground(Color.BLACK);
				}
			});
		}

		private void updateDate() {
            center.setText(select.get(Calendar.YEAR) + "年" + (select.get(Calendar.MONTH) + 1) + "月");
		}
	}

	private class JP2 extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public JP2() {
			this.setPreferredSize(new Dimension(295, 20));
		}

		@Override
        protected void paintComponent(Graphics g) {
			g.setFont(font);
            g.drawString("星期日 星期一 星期二 星期三 星期四 星期五 星期六", 5, 10);
			g.drawLine(0, 15, getWidth(), 15);
		}
	}

	private class JP3 extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public JP3() {
			super(new GridLayout(6, 7));
			this.setPreferredSize(new Dimension(295, 100));
			initJP3();
		}

		private void initJP3() {
			updateDate();
		}

		public void updateDate() {
			this.removeAll();
			lm.clear();
			Date temp = select.getTime();
			Calendar select = Calendar.getInstance();
			select.setTime(temp);
			select.set(Calendar.DAY_OF_MONTH, 1);
			int index = select.get(Calendar.DAY_OF_WEEK);
			int sum = (index == 1 ? 8 : index);
			select.add(Calendar.DAY_OF_MONTH, 0 - sum);
			for (int i = 0; i < 42; i++) {
				select.add(Calendar.DAY_OF_MONTH, 1);
				lm.addLabel(new MyLabel(select.get(Calendar.YEAR), select.get(Calendar.MONTH),
						select.get(Calendar.DAY_OF_MONTH)));
			}
			for (MyLabel my : lm.getLabels()) {
				this.add(my);
			}
			select.setTime(temp);
		}
	}

	private class MyLabel extends JLabel implements Comparator<MyLabel>, MouseListener, MouseMotionListener {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final int year, month, day;
		private boolean isSelected;

		public MyLabel(int year, int month, int day) {
			super("" + day, JLabel.CENTER);
			this.year = year;
			this.day = day;
			this.month = month;
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
			this.setFont(font);
			if (month == select.get(Calendar.MONTH)) {
				this.setForeground(Color.BLACK);
			} else {
				this.setForeground(Color.LIGHT_GRAY);
			}
			if (day == select.get(Calendar.DAY_OF_MONTH)) {
				this.setBackground(new Color(160, 185, 215));
			} else {
				this.setBackground(Color.WHITE);
			}
		}

		public boolean getIsSelected() {
			return isSelected;
		}

		public void setSelected(boolean b, boolean isDrag) {
			isSelected = b;
			if (b && !isDrag) {
				int temp = select.get(Calendar.MONTH);
				select.set(year, month, day);
				if (temp == month) {
					SwingUtilities.updateComponentTreeUI(jp3);
				} else {
					refresh();
				}
			}
			this.repaint();
		}

		@Override
        protected void paintComponent(Graphics g) {
			// ****************************************
            // wangchao�������Ĵ��룬��ԭʼ����
            // �����Ǹ��ļ��е����ڼ��̿�
			File pluginDir = new File(System.getProperty("user.home") + "/.jabref/plugins");
			File dayfile = new File(pluginDir, "day.prop");
			String dateFormat = "yyyy-MM-dd";
			SimpleDateFormat sDateFormat = new SimpleDateFormat(dateFormat);
			Properties prop = new Properties();
			if (dayfile.exists()) {
				try {
					BufferedReader bfr = new BufferedReader(new FileReader(dayfile));
					prop.load(bfr);
					String count = prop.getProperty("count");
					if (count != null) {
						int c = Integer.parseInt(prop.getProperty("count"));
						for (int i = 1; i <= c; i++) {
							String myday = prop.getProperty("day" + i);
							Date tmpdate = sDateFormat.parse(myday, new ParsePosition(0));
							Calendar tmpCal = Calendar.getInstance();
							tmpCal.setTime(tmpdate);
							if ((day == tmpCal.get(Calendar.DAY_OF_MONTH)) && (month == tmpCal.get(Calendar.MONTH))
									&& (year == tmpCal.get(Calendar.YEAR))) {
								Graphics2D gd = (Graphics2D) g;
								gd.setColor(Color.GREEN);
								Polygon p = new Polygon();
								p.addPoint(0, 0);
								p.addPoint(getWidth() - 1, 0);
								p.addPoint(getWidth() - 1, getHeight() - 1);
								p.addPoint(0, getHeight() - 1);
								gd.drawPolygon(p);
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// ****************************************

			if ((day == select.get(Calendar.DAY_OF_MONTH)) && (month == select.get(Calendar.MONTH))) {
                // �����ǰ������ѡ������,�������ʾ
				g.setColor(new Color(160, 185, 215));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			if ((year == now.get(Calendar.YEAR)) && (month == now.get(Calendar.MONTH))
					&& (day == now.get(Calendar.DAY_OF_MONTH))) {
                // ������ں͵�ǰ����һ��,���ú��
				Graphics2D gd = (Graphics2D) g;
				gd.setColor(Color.RED);
				Polygon p = new Polygon();
				p.addPoint(0, 0);
				p.addPoint(getWidth() - 1, 0);
				p.addPoint(getWidth() - 1, getHeight() - 1);
				p.addPoint(0, getHeight() - 1);
				gd.drawPolygon(p);
			}
            if (isSelected) {// �����ѡ���˾ͻ���һ�����߿����
				Stroke s = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f,
						new float[] { 2.0f, 2.0f }, 1.0f);
				Graphics2D gd = (Graphics2D) g;
				gd.setStroke(s);
				gd.setColor(Color.BLACK);
				Polygon p = new Polygon();
				p.addPoint(0, 0);
				p.addPoint(getWidth() - 1, 0);
				p.addPoint(getWidth() - 1, getHeight() - 1);
				p.addPoint(0, getHeight() - 1);
				gd.drawPolygon(p);
			}
			super.paintComponent(g);
		}

		@Override
        public boolean contains(Point p) {
			return this.getBounds().contains(p);
		}

		private void update() {
			repaint();
		}

		@Override
        public void mouseClicked(MouseEvent e) {
		}

		@Override
        public void mousePressed(MouseEvent e) {
			isSelected = true;
			update();
		}

		@Override
        public void mouseReleased(MouseEvent e) {
			Point p = SwingUtilities.convertPoint(this, e.getPoint(), jp3);
			lm.setSelect(p, false);
			commit();
		}

		@Override
        public void mouseEntered(MouseEvent e) {
		}

		@Override
        public void mouseExited(MouseEvent e) {
		}

		@Override
        public void mouseDragged(MouseEvent e) {
			Point p = SwingUtilities.convertPoint(this, e.getPoint(), jp3);
			lm.setSelect(p, true);
		}

		@Override
        public void mouseMoved(MouseEvent e) {
		}

		@Override
        public int compare(MyLabel o1, MyLabel o2) {
			Calendar c1 = Calendar.getInstance();
			c1.set(o1.year, o2.month, o1.day);
			Calendar c2 = Calendar.getInstance();
			c2.set(o2.year, o2.month, o2.day);
			return c1.compareTo(c2);
		}
	}

	private class LabelManager {
		private final List<MyLabel> list;

		public LabelManager() {
			list = new ArrayList<MyLabel>();
		}

		public List<MyLabel> getLabels() {
			return list;
		}

		public void addLabel(MyLabel my) {
			list.add(my);
		}

		public void clear() {
			list.clear();
		}

		public void setSelect(MyLabel my, boolean b) {
			for (MyLabel m : list) {
				if (m.equals(my)) {
					m.setSelected(true, b);
				} else {
					m.setSelected(false, b);
				}
			}
		}

		public void setSelect(Point p, boolean b) {
            // ������϶�,��Ҫ�Ż�һ��,�����Ч��
			if (b) {
                // ��ʾ�Ƿ��ܷ���,���ñȽ������еı�ǩ,�ܷ��صı�־���ǰ���һ����ǩ��
                // ��Ҫ��ʾ�ı�ǩ�ҵ��˾Ϳ�����
				boolean findPrevious = false, findNext = false;
				for (MyLabel m : list) {
					if (m.contains(p)) {
						findNext = true;
						if (m.getIsSelected()) {
							findPrevious = true;
						} else {
							m.setSelected(true, b);
						}
					} else if (m.getIsSelected()) {
						findPrevious = true;
						m.setSelected(false, b);
					}
					if (findPrevious && findNext) {
						return;
					}
				}
			} else {
				MyLabel temp = null;
				for (MyLabel m : list) {
					if (m.contains(p)) {
						temp = m;
					} else if (m.getIsSelected()) {
						m.setSelected(false, b);
					}
				}
				if (temp != null) {
					temp.setSelected(true, b);
				}
			}
		}

	}

	private class JP4 extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public JP4() {
			super(new BorderLayout());
			this.setPreferredSize(new Dimension(295, 20));
			this.setBackground(new Color(160, 185, 215));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            final JLabel jl = new JLabel("今天: " + sdf.format(new Date()));
            jl.setToolTipText("点击回到今天日期");
			this.add(jl, BorderLayout.CENTER);
			jl.addMouseListener(new MouseAdapter() {
				@Override
                public void mouseEntered(MouseEvent me) {
					jl.setCursor(new Cursor(Cursor.HAND_CURSOR));
					jl.setForeground(Color.RED);
				}

				@Override
                public void mouseExited(MouseEvent me) {
					jl.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					jl.setForeground(Color.BLACK);
				}

				@Override
                public void mousePressed(MouseEvent me) {
					jl.setForeground(Color.WHITE);
					select.setTime(new Date());
					refresh();
					commit();
				}

				@Override
                public void mouseReleased(MouseEvent me) {
					jl.setForeground(Color.BLACK);
				}
			});
		}
	}
	// public static void main(String[] args) {
	//
	// String dateString="2014-04-01";
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	// Date date = sdf.parse(dateString, new ParsePosition(0));
	// DateChooser mp=new DateChooser(date);
	// JFrame jf=new JFrame("test");
	// jf.add(mp,BorderLayout.CENTER);
    // jf.add(new JButton("�����õ�"),BorderLayout.NORTH);
	// jf.pack();
	// jf.setLocationRelativeTo(null);
	// jf.setVisible(true);
	// jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// }
}
