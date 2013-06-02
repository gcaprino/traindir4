package com.traindirector.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.traindirector.net.HTTPClient;
import com.traindirector.model.Train;

public class EditScheduleView extends ViewPart {

	public static final String ID = "com.bstreet.traindirector.views.editschedule";
	public static final String EDIT_SCHEDULE_PREFERENCE = "com.bstreet.traindirector.prefs.editSchedule";
	
	public static final String DEFAULT_URL = "http://www.e656.net/orario/stazioni.html";

	Browser _browser;
	String _currentLocation;
	Text _currentLocationText;
	Button _addLocationButton;
	Button _saveListButton;
	Button _createScheduleButton;
	String _savedFile;
	List<String> _locationList;

	FileDialog saveDialog = null;
	FileDialog saveSchedDialog = null;

	List<String> stationsPages;
	List<Train> trains;
	
//	TreeItem	_project;
//	TreeItem	_pagesItem;

	@Override
	public void createPartControl(Composite parent) {

		//_projectView = WorkspaceView.getInstance();
//		_project = _projectView.getScheduleEditorItem();
//		_pagesItem = _projectView.addOrCreate(_project, "Web Pages");

		_locationList = new ArrayList<String>();
		stationsPages = new ArrayList<String>();
		trains = new ArrayList<Train>();

		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);

		Composite topBar = new Composite(top, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginHeight = 0;
		rowLayout.marginWidth = 0;
		topBar.setLayout(rowLayout);

		Label label = new Label(topBar, SWT.NONE);
		label.setText("Current Location: ");

		_currentLocationText = new Text(topBar, SWT.READ_ONLY);
		_currentLocationText.setLayoutData(new RowData(400, 20));

		_addLocationButton = new Button(topBar, SWT.PUSH);
		_addLocationButton.setText("Add To List");
		_addLocationButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				addCurrentLocationToList();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addCurrentLocationToList();
			}

		});

		_saveListButton = new Button(topBar, SWT.PUSH);
		_saveListButton.setText("Save List");
		_saveListButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveList();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				saveList();
			}

		});

		_createScheduleButton = new Button(topBar, SWT.PUSH);
		_createScheduleButton.setText("Create Schedule");
		_createScheduleButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				createSchedule();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				createSchedule();
			}

		});

/*
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					int i = _locationListWidget.getSelectionIndex();
					String s = _locationList.get(i);
					_locationListWidget.remove(i);
					_locationList.remove(i);
				}
			}
*/
		
		Composite linksBar = new Composite(top, SWT.NONE);
		rowLayout = new RowLayout();
		rowLayout.marginHeight = 0;
		rowLayout.marginWidth = 0;
		linksBar.setLayout(rowLayout);

		String linkString = "";
		char ch = 'A';
		for(ch = 'A'; ch <= 'Z'; ++ch) {
			linkString += "<a>" + ch + "</a>  ";
		}
		Link lnk = new Link(linksBar, SWT.NONE);
		lnk.setText(linkString);
		lnk.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = e.text.toLowerCase();
				String url = "http://www.e656.net/orario/stazioni/" + s + ".html";
				_browser.setUrl(url);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});

		_browser = new Browser(top, SWT.NONE);
		_browser.setLayoutData(new GridData(GridData.FILL_BOTH));


		_browser.addLocationListener(new LocationListener() {

			@Override
			public void changing(LocationEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void changed(LocationEvent event) {
				_currentLocation = event.location;
				_currentLocationText.setText(_currentLocation);
			}

		});
		String url = DEFAULT_URL;
        Preferences preferences = ConfigurationScope.INSTANCE
                .getNode(EDIT_SCHEDULE_PREFERENCE);
        Preferences sub1 = preferences.node("scheduleEditor");
        _savedFile = sub1.get("savedFile", null);
        if (_savedFile != null && !_savedFile.isEmpty()) {
        	loadList();
        	if (_locationList.size() > 0) {
        		url = _locationList.get(_locationList.size() - 1);
        	}
        }
        _browser.setUrl(url);
	}

	@Override
	public void setFocus() {
	}

	private void addCurrentLocationToList() {
		for (String s : _locationList) {
			if (s.compareTo(_currentLocation) == 0) {
				return;
			}
		}
		_locationList.add(_currentLocation);

//		TreeItem node = new TreeItem(_pagesItem, SWT.NONE);
//		node.setText(_currentLocation);
	}

	private void loadList() {
		File file = new File(_savedFile);
		if (!file.canRead()) {
			return;
		}
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				_locationList.add(s);
//				_projectView.addOrCreate(_pagesItem, s);
			}
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void saveList() {
		if (saveDialog == null) {
			saveDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			saveDialog.setFileName(_savedFile);
		}
		String fileName = saveDialog.open();
		if (fileName == null)
			return;
		File file = new File(fileName);
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fw);
			for (String s : _locationList) {
				writer.write(s);
				writer.write("\n");
			}
			writer.close();
		} catch (Exception e) {
			MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell());
			mb.setText("Error " + e.getMessage());
			mb.open();
		}
		
        Preferences preferences = ConfigurationScope.INSTANCE
                .getNode(EDIT_SCHEDULE_PREFERENCE);
            Preferences pref = preferences.node("scheduleEditor");
            pref.put("savedFile", fileName);

            try {
              // Forces the application to save the preferences
              preferences.flush();
            } catch (BackingStoreException e2) {
              e2.printStackTrace();
            }
	}

	private void createSchedule() {
		if (saveSchedDialog == null) {
			saveSchedDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			saveSchedDialog.setFileName(_savedFile);
		}
		final String fileName = saveSchedDialog.open();
		if (fileName == null)
			return;

		Runnable r = new Runnable() {
			public void run() {
				createScheduleJob(fileName);
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	private void createScheduleJob(String fileName) {
		stationsPages.clear();
		trains.clear();
		int row = -1;
		for (String url : _locationList) {
			++row;
			int i = url.lastIndexOf('/');
			if (i < 0)
				continue;
			String pageName = url.substring(i + 1);
			String pageBase = "http://www.e656.net";
			pageName = Platform.getLocation().toOSString() + File.separatorChar + pageName;
			try {
				HTTPClient client = new HTTPClient();
				String result = client.get(url);
				if (result != null) {
					System.out.println(result);
					File file = new File(pageName);
					FileWriter fw = new FileWriter(file);
					BufferedWriter writer = new BufferedWriter(fw);
					writer.write(result);
					fw.close();
					stationsPages.add(pageName);
					FileReader fr = new FileReader(file);
					BufferedReader reader = new BufferedReader(fr);
					analyzePage(result, pageBase);
					// TODO: update number of train in tree node
				}
				Thread.sleep(1000);
				/*
				_browser.setUrl(url);
				String result = _browser.getText();
				if (result != null) {
					System.out.println(result);
					File file = new File(pageName);
					FileWriter fw = new FileWriter(file);
					BufferedWriter writer = new BufferedWriter(fw);
					writer.write(result);
					fw.close();
				}
				 */
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		createScheduleFromTrainList(fileName);
	}
	
	public Train findTrainFromUrl(String name) {
		// TODO: use map
		/*
		for(Train train : trains) {
			if (train._url.compareTo(name) == 0) {
				return train;
			}
		}
		*/
		return null;
	}
	public int analyzePage(BufferedReader rdr) {
		return 0;
	}
	public int analyzePage(String content, String pageBase) {
		int indx = 0;
		int l = 0;
		int count = 0;
		/*
		while((indx = content.indexOf("/orario/treno/", indx)) >= 0) {
			StringBuilder sb = new StringBuilder();
			while(content.charAt(indx) != '\'') {
				sb.append(content.charAt(indx));
				++indx;
			}
			Train train = findTrainFromUrl(sb.toString());
			if (train == null) {
				String name = 
				train = new Train();
				train._url = pageBase + sb.toString();
				trains.add(train);
				l = train._url.lastIndexOf('/');
				if(l >= 0) {
					train._name = train._url.substring(l + 1);
				}
				++count;
			}
			int x;
			l = content.indexOf("<td>", indx);
			for(x = 0; x < 2; ++x) {
				l = content.indexOf("<td>", l + 1);
				if (l < 0)
					break;
			}
			if(l< 0)
				continue;
			sb = new StringBuilder();
			for(l += 4; content.charAt(l) != '<'; ++l)
				sb.append(content.charAt(l));
			train._atime = sb.toString();
			l = content.indexOf("<td>", l);
			if (l < 0) {
				continue;
			}
			sb = new StringBuilder();
			for(l += 4; content.charAt(l) != '<'; ++l)
				sb.append(content.charAt(l));
			train._dtime = sb.toString();
		}
		*/
		return count;
	}

	public void createScheduleFromTrainList(String destFileName) {
		String schName = Platform.getLocation().toOSString() + File.separatorChar + "schedule.sch";
		schName = destFileName;
		File fileSch = new File(schName);
		FileWriter fwSch;
		try {
			fwSch = new FileWriter(fileSch);
			BufferedWriter writerSch = new BufferedWriter(fwSch);
			writerSch.write("#!trdir\n\n");
			for (Train train : trains) {
				/* TODO
				if (train._url == null) {
					continue;
				}
				String trainPageName = Platform.getLocation().toOSString() + File.separatorChar + train._name;
	
				HTTPClient client = new HTTPClient();
				String result = client.get(train._url);
				if (result == null) {
					continue;
				}
				System.out.println(result);
				File file = new File(trainPageName);
				FileWriter fw = new FileWriter(file);
				BufferedWriter writer = new BufferedWriter(fw);
				writer.write(result);
				fw.close();
				parseTrainPage(train, result, writerSch);
				Thread.sleep(1000);
				*/
			}
			fwSch.close();
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Information", "Schedule file creation finished.");
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			/* TODO
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			*/
		}
	}
	
	public void parseTrainPage(Train train, String page, BufferedWriter writerSch) throws IOException {
		int indx = train._name.lastIndexOf('.');
		String name = train._name.substring(0, indx);
		StringBuilder sb = new StringBuilder();
		sb.append("Train: ");
		sb.append(name);
		sb.append("\n");
		indx = page.indexOf("Stazione d'arrivo");
		if (indx < 0) {
			
		}
		while((indx = page.indexOf("5px;'>", indx)) > 0) {
			indx += 6;
			StringBuilder stat = new StringBuilder();
			while(page.charAt(indx) != '<') {
				stat.append(page.charAt(indx));
				++indx;
			}
			indx = page.indexOf("E6;'>", indx) + 5;
			StringBuilder arr = new StringBuilder();
			while(page.charAt(indx) != '<') {
				arr.append(page.charAt(indx));
				++indx;
			}
			indx = page.indexOf("E6;'>", indx) + 5;
			StringBuilder dep = new StringBuilder();
			while(page.charAt(indx) != '<') {
				dep.append(page.charAt(indx));
				++indx;
			}
			String arrStr = convert_time(arr.toString());
			String depStr = convert_time(dep.toString());
			String statStr = convert_station(stat.toString());
			if(arrStr.startsWith("----") || arrStr.startsWith("- - -")) {
				sb.append("Enter: ");
				sb.append(depStr);
				sb.append(',');
				sb.append(statStr);
				sb.append('\n');
			} else if(depStr.startsWith("----") || arrStr.startsWith("- - -")) {
				sb.append("\t");
				sb.append(arrStr);
				sb.append(", -, ");
				sb.append(statStr);
				sb.append('\n');
				break;
			} else {
				sb.append("\t");
				sb.append(arrStr);
				sb.append(", ");
				sb.append(depStr);
				sb.append(", ");
				sb.append(statStr);
				sb.append("\n");
			}
		}
		sb.append(".\n\n");
		System.out.println(sb.toString());
		writerSch.write(sb.toString());
	}
	
	public String convert_time(String s) {
		if(s.charAt(2) == '.') {
			s = s.substring(0, 2) + ':' + s.substring(3);
		}
		if(s.charAt(5) == '.') {
			s = s.substring(0, 5) + ':' + s.substring(6);
		}
		return s;
	}
	
	public String convert_station(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); ) {
			sb.append(s.charAt(i++));		// keep first character capitalized
			while(i < s.length() && s.charAt(i) >= 'A' && s.charAt(i) <= 'Z') {
				sb.append(s.substring(i, i + 1).toLowerCase());
				++i;
			}
			while(i < s.length() && !(s.charAt(i) >= 'A' && s.charAt(i) <= 'Z')) {
				sb.append(s.substring(i, i + 1).toLowerCase());
				++i;
			}
		}
		return sb.toString();
	}
}
