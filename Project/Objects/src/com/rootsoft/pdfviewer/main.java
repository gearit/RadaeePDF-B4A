package com.rootsoft.pdfviewer;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "com.rootsoft.pdfviewer", "com.rootsoft.pdfviewer.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "com.rootsoft.pdfviewer", "com.rootsoft.pdfviewer.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "com.rootsoft.pdfviewer.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static int _result = 0;
public com.rootsoft.pdfviewer.readercontroller _vpdf = null;
public com.rootsoft.rspdfviewer.pdf.RSPDFGlobal _global = null;
public com.rootsoft.rspdfviewer.pdf.RSPDFDocument _document = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 45;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 49;BA.debugLine="Global.Initialize(\"PDFViewer\")";
mostCurrent._global.Initialize(mostCurrent.activityBA,"PDFViewer");
 //BA.debugLineNum = 50;BA.debugLine="vPDF.Initialize(\"ReaderController\")";
mostCurrent._vpdf._initialize(mostCurrent.activityBA,"ReaderController");
 //BA.debugLineNum = 51;BA.debugLine="Document.Initialize";
mostCurrent._document.Initialize();
 //BA.debugLineNum = 52;BA.debugLine="Document.Close";
mostCurrent._document.Close();
 //BA.debugLineNum = 54;BA.debugLine="CopyFile";
_copyfile();
 //BA.debugLineNum = 56;BA.debugLine="result = Document.Open(File.Combine(File.DirRootExternal, \"test.pdf\"), Null)";
_result = mostCurrent._document.Open(anywheresoftware.b4a.keywords.Common.File.Combine(anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"test.pdf"),BA.ObjectToString(anywheresoftware.b4a.keywords.Common.Null));
 //BA.debugLineNum = 59;BA.debugLine="If CheckResult(result) Then";
if (_checkresult(_result)) { 
 //BA.debugLineNum = 60;BA.debugLine="ToastMessageShow(\"Opening document.\", False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Opening document.",anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 61;BA.debugLine="vPDF.Open(Document)";
mostCurrent._vpdf._open(mostCurrent._document);
 //BA.debugLineNum = 62;BA.debugLine="Activity.AddView(vPDF.AsView, 0dip, 0dip, 100%x, 100%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._vpdf._asview().getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA));
 //BA.debugLineNum = 63;BA.debugLine="vPDF.Invalidate";
mostCurrent._vpdf._invalidate();
 //BA.debugLineNum = 64;BA.debugLine="vPDF.AsView.BringToFront";
mostCurrent._vpdf._asview().BringToFront();
 }else {
 //BA.debugLineNum = 66;BA.debugLine="ToastMessageShow(\"An error occurred.: \" & result, True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("An error occurred.: "+BA.NumberToString(_result),anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 70;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 76;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 77;BA.debugLine="Close";
_close();
 //BA.debugLineNum = 78;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 72;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 74;BA.debugLine="End Sub";
return "";
}
public static boolean  _checkresult(int _presult) throws Exception{
 //BA.debugLineNum = 80;BA.debugLine="Sub CheckResult (pResult As Int) As Boolean";
 //BA.debugLineNum = 82;BA.debugLine="Select pResult";
switch (BA.switchObjectToInt(_presult,(int) (0),(int) (-1),(int) (-2),(int) (-3),(int) (-10))) {
case 0:
 //BA.debugLineNum = 84;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 break;
case 1:
 break;
case 2:
 break;
case 3:
 break;
case 4:
 break;
}
;
 //BA.debugLineNum = 92;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 93;BA.debugLine="End Sub";
return false;
}
public static String  _close() throws Exception{
 //BA.debugLineNum = 95;BA.debugLine="Sub Close";
 //BA.debugLineNum = 96;BA.debugLine="vPDF.Close";
mostCurrent._vpdf._close();
 //BA.debugLineNum = 97;BA.debugLine="If Document <> Null Then";
if (mostCurrent._document!= null) { 
 //BA.debugLineNum = 98;BA.debugLine="Document.Close";
mostCurrent._document.Close();
 };
 //BA.debugLineNum = 100;BA.debugLine="Global.RemoveTmp";
mostCurrent._global.RemoveTmp();
 //BA.debugLineNum = 101;BA.debugLine="End Sub";
return "";
}
public static String  _copyfile() throws Exception{
 //BA.debugLineNum = 103;BA.debugLine="Sub CopyFile";
 //BA.debugLineNum = 104;BA.debugLine="If Not(File.Exists(File.DirRootExternal, \"test.pdf\")) Then";
if (anywheresoftware.b4a.keywords.Common.Not(anywheresoftware.b4a.keywords.Common.File.Exists(anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"test.pdf"))) { 
 //BA.debugLineNum = 105;BA.debugLine="File.Copy(File.DirAssets, \"test.pdf\", File.DirRootExternal, \"test.pdf\")";
anywheresoftware.b4a.keywords.Common.File.Copy(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"test.pdf",anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"test.pdf");
 };
 //BA.debugLineNum = 107;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _globals() throws Exception{
 //BA.debugLineNum = 25;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 30;BA.debugLine="Dim result As Int";
_result = 0;
 //BA.debugLineNum = 37;BA.debugLine="Dim vPDF As ReaderController";
mostCurrent._vpdf = new com.rootsoft.pdfviewer.readercontroller();
 //BA.debugLineNum = 38;BA.debugLine="Dim Global As PDFGlobal";
mostCurrent._global = new com.rootsoft.rspdfviewer.pdf.RSPDFGlobal();
 //BA.debugLineNum = 39;BA.debugLine="Dim Document As PDFDocument";
mostCurrent._document = new com.rootsoft.rspdfviewer.pdf.RSPDFDocument();
 //BA.debugLineNum = 43;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 23;BA.debugLine="End Sub";
return "";
}
}
