package DownloadManager;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class DownloadManager extends JFrame implements Observer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3132040012603981264L;
	private JTextField addTextField;
	private DownloadsTableModel tableModel;
	private JTable table;
	private JButton pauseButton,resumeButton;
	private JButton cancelButton,clearButton;
	private Download selectedDownload;
	private boolean clearing;
	
	public DownloadManager()
	{
	setTitle("Download Manager");
	setSize(480,240);
	addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
			actionExit();
			}
		});
	JMenuBar menubar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);
	JMenuItem fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
	fileExitMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			actionExit();
			}
		});
	fileMenu.add(fileExitMenuItem);
	menubar.add(fileMenu);
	setJMenuBar(menubar);

	JPanel addPanel = new JPanel();
	addTextField = new JTextField(30);
	addPanel.add(addTextField);
	
	JButton addButton = new JButton("Add Download");
	addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			actionAdd();
			}
		});
	addPanel.add(addButton);
	
	tableModel = new DownloadsTableModel();
	table = new JTable(tableModel);
	table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
			tableSelectionChanged();
                        }
		});
	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
	ProgressRenderer renderer = new ProgressRenderer(0,100);
	renderer.setStringPainted(true);
	table.setDefaultRenderer(JProgressBar.class,renderer);
	table.setRowHeight((int) renderer.getPreferredSize().getHeight());
	
	JPanel downloadPanel = new JPanel();
	downloadPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
	downloadPanel.setLayout(new BorderLayout());
	downloadPanel.add(new JScrollPane(table),BorderLayout.CENTER);
	
	JPanel buttonPanel = new JPanel();
	pauseButton = new JButton("Pause");
	pauseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			actionPause();
			}
		});
	pauseButton.setEnabled(false);
	buttonPanel.add(pauseButton);

	resumeButton = new JButton("Resume");
	resumeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			actionResume();
			}
		});
	resumeButton.setEnabled(false);
	buttonPanel.add(resumeButton);
	
	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener()	
		{
			public void actionPerformed(ActionEvent e)
			{
			actionCancel();
			}
		});
	cancelButton.setEnabled(false);
	buttonPanel.add(cancelButton);
	
	clearButton  = new JButton("Clear");
	clearButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			actionClear();
			}
		});
	clearButton.setEnabled(false);
	buttonPanel.add(clearButton);
	
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(addPanel,BorderLayout.NORTH);
	getContentPane().add(downloadPanel,BorderLayout.CENTER);
	getContentPane().add(buttonPanel,BorderLayout.SOUTH);
	}
	private void actionExit()
	{
	System.exit(0);
	}
	private void actionAdd()
	{
	URL verifiedUrl = verifyUrl(addTextField.getText());
		if(verifiedUrl != null)
		{
		tableModel.addDownload(new Download(verifiedUrl));
		addTextField.setText("");
		}
		else
		{
		JOptionPane.showMessageDialog(this,"Invalid Download Url","Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	private URL verifyUrl(String url)
	{
		if(!url.toLowerCase().startsWith("https://"))
		{
		return null;
		}
	URL verifiedUrl =null;
		try
		{
		verifiedUrl  = new URL(url);
		}
		catch(Exception e)
		{
		return null;
		}
		if(verifiedUrl.getFile().length()<2)
                {
		return null;
                }
        return verifiedUrl;
	}
	private void tableSelectionChanged()
	{
		if(selectedDownload != null)
		selectedDownload.deleteObserver(DownloadManager.this);
		
		if(!clearing && table.getSelectedRow() > -1)
		{
		selectedDownload = tableModel.getDownload(table.getSelectedRow());
		selectedDownload.addObserver(DownloadManager.this);
		updateButtons();
		}
	}
	private void actionPause()
	{
	selectedDownload.pause();
	updateButtons();
	}
	private void actionResume()
	{
	selectedDownload.resume();
	updateButtons();
	}
	private void actionCancel()
	{
	selectedDownload.cancel();
	updateButtons();
	}
	private void actionClear()
	{
	clearing=true;
	tableModel.clearDownload(table.getSelectedRow());
	clearing=false;
	selectedDownload = null;
	updateButtons();
	}
	private void updateButtons()
	{
		if(selectedDownload !=null)
		{
		int status = selectedDownload.getStatus();
			switch(status)
			{
			case Download.DOWNLOADING:
			pauseButton.setEnabled(true);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(true);
			clearButton.setEnabled(false);
			break;
			
			case Download.PAUSED:
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(true);
			cancelButton.setEnabled(true);
			clearButton.setEnabled(false);
			break;
			
			case Download.ERROR:
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(true);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(true);
			break;	
			
			default: // COMPLETE OR CANCELED
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(true);
			}	
		}
		else
		{
		pauseButton.setEnabled(false);
		resumeButton.setEnabled(false);
		cancelButton.setEnabled(false);
		clearButton.setEnabled(false);
		}
	}
	public void update(Observable o,Object arg)
	{
		if(selectedDownload !=null && selectedDownload.equals(o))
		updateButtons();
	}
	public static void main(String arg[])
	{
	DownloadManager manager = new DownloadManager();
	manager.setVisible(true);
	}
}