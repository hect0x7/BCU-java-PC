package main;

import common.CommonStatic;
import common.battle.BasisSet;
import common.io.Backup;
import common.io.PackLoader.ZipDesc.FileDesc;
import common.io.assets.Admin;
import common.io.assets.AssetLoader;
import common.pack.Context;
import common.pack.Context.ErrType;
import common.pack.Source;
import common.pack.Source.Workspace;
import common.pack.UserProfile;
import common.system.DateComparator;
import common.system.fake.ImageBuilder;
import common.system.files.VFile;
import common.util.AnimGroup;
import common.util.Data;
import common.util.stage.Replay;
import io.BCJSON;
import io.BCUReader;
import io.BCUWriter;
import jogl.GLBBB;
import jogl.util.GLIB;
import org.jetbrains.annotations.NotNull;
import page.*;
import page.awt.AWTBBB;
import page.awt.BBBuilder;
import page.battle.BattleBox;
import utilpc.Theme;
import utilpc.UtilPC;
import utilpc.awt.FIBI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class MainBCU {

	public static class AdminContext implements Context {

		@Override
		public boolean confirmDelete() {
			return Opts.conf();
		}

		@Override
		public boolean confirmDelete(File f) {
			return Opts.conf(f);
		}

		@Override
		public File getAssetFile(String string) {
			return new File(getBCUFolder(), "./assets/" + string);
		}

		@Override
		public File getAuxFile(String path) {
			return new File(getBCUFolder(), path);
		}

		@Override
		public InputStream getLangFile(String file) {
			File f = new File(getBCUFolder(),
					"./assets/lang/" + CommonStatic.Lang.LOC_CODE[CommonStatic.getConfig().lang] + "/" + file);
			if (!f.exists()) {
				String path = "common/util/lang/assets/" + file;
				return ClassLoader.getSystemResourceAsStream(path);
			}
			return Data.err(() -> new FileInputStream(f));
		}

		@Override
		public File getUserFile(String string) {
			return new File(getBCUFolder(), "./user/" + string);
		}

		@Override
		public File getWorkspaceFile(String relativePath) {
			return new File(getBCUFolder(), "./workspace/" + relativePath);
		}

		@Override
		public File getBackupFile(String string) {
			return new File(getBCUFolder(), "./backups/"+string);
		}

		@Override
		public String getAuthor() {
			return author;
		}

		@Override
		public void initProfile() {
			LoadPage.prog("reading assets");
			AssetLoader.load(LoadPage::prog);
			LoadPage.prog("reading BC data");
			UserProfile.getBCData().load(LoadPage::prog, LoadPage::prog);
			LoadPage.prog("reading backups");
			Backup.loadBackups();

			Backup restore = Backup.checkRestore();

			if(restore != null) {
				LoadPage.prog("restoring data");
				boolean result = CommonStatic.ctx.restore(restore, LoadPage::prog);

				if(!result) {
					Opts.pop("Failed to restore data", "Restoration failed");
				}
			}

			LoadPage.prog("reading local animations");
			Workspace.loadAnimations(null);
			LoadPage.prog("reading local animation group data");
			AnimGroup.readGroupData();
			LoadPage.prog("reading packs");
			UserProfile.loadPacks(LoadPage::prog);
			LoadPage.prog("reading basis");
			BasisSet.read();
			LoadPage.prog("reading replays");
			Replay.read();
			LoadPage.prog("remove old files");
			CommonStatic.ctx.noticeErr(() -> {
				Context.delete(new File(getBCUFolder(), "./user/backup.zip"));
				Context.delete(new File(getBCUFolder(), "./user/basis.v"));
				Context.delete(new File(getBCUFolder(), "./user/data.ini"));
				Context.delete(new File(getBCUFolder(), "./assets/assets.zip"));
				Context.delete(new File(getBCUFolder(), "./assets/calendar/"));
			}, ErrType.WARN, "Failed to delete old files");
			LoadPage.prog("finished reading");
		}

		@Override
		public void noticeErr(Exception e, ErrType t, String str) {
			if (noNeedToShow(t)) {
				System.out.println(str);
				e.printStackTrace();
				return;
			}
			e.printStackTrace(t == ErrType.INFO ? System.out : System.err);
			printErr(t, str);
		}

		@Override
		public boolean preload(FileDesc desc) {
			return Admin.preload(desc);
		}

		@Override
		public void printErr(ErrType t, String str) {
			if (noNeedToShow(t)) {
				System.out.println(str);
				return;
			}

			(t == ErrType.INFO ? System.out : System.err).println(str);

			if (t != ErrType.INFO)
				Opts.errOnce(str, "ERROR", t == ErrType.FATAL || t == ErrType.ERROR);
		}

		@Override
		public void loadProg(String str) {
			LoadPage.prog(str);
		}

		private boolean noNeedToShow(ErrType t) {
			if(t == ErrType.DEBUG)
				return true;
			else
				return !(t == ErrType.CORRUPT || t == ErrType.ERROR || t == ErrType.FATAL || t == ErrType.WARN || !MainBCU.WRITE);
		}

		@Override
		public boolean restore(Backup b, Consumer<Double> prog) {
			if(CommonStatic.getConfig().backupPath == null) {
				File packs = CommonStatic.ctx.getAuxFile("./packs");
				File workspace = CommonStatic.ctx.getWorkspaceFile("");
				File user = CommonStatic.ctx.getUserFile("");

				try {
					if(packs.exists())
						Context.delete(packs);

					if(workspace.exists())
						Context.delete(workspace);

					if(user.exists())
						Context.delete(user);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}

				boolean result = CommonStatic.ctx.noticeErr(() -> b.backup.unzip(path -> new File(getBCUFolder(), "./"+path), prog), ErrType.ERROR, "Failed to restore files");

				DateComparator comparator = new DateComparator();

				Backup.backups.removeIf(ba -> comparator.compare(b, ba) < 1 && ba.safeDelete());

				return result;
			} else {
				VFile vf = b.backup.tree.find(CommonStatic.getConfig().backupPath);

				CommonStatic.getConfig().backupPath = null;

				if(vf == null)
					return false;

				try {
					extractData(vf, prog);
				} catch (Exception e) {
					return false;
				}

				return true;
			}
		}

		@NotNull
		@Override
		public File getBCUFolder() {
			if(!MainBCU.WRITE)
				return new File("./");

			try {
				File f = new File(MainBCU.class.getProtectionDomain().getCodeSource().getLocation().toURI());

				File parent = f.getParentFile();

				if(parent != null) {
					return parent;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new File("./");
		}

		private void extractData(VFile vf, Consumer<Double> prog) throws IOException {
			if(vf.getData() != null) {
				double max = vf.getData().size();
				double progress = 0;

				File f = new File(vf.getPath());

				if(!f.exists() && !f.createNewFile()) {
					return;
				}

				FileOutputStream fos = new FileOutputStream(f);
				InputStream ins = vf.getData().getStream();
				byte[] b = new byte[65536];
				int len;

				while((len = ins.read(b)) != -1) {
					fos.write(b, 0, len);
					progress += len;

					prog.accept(progress/max);
				}

				fos.close();
			} else {
				int num = getFileNumber(vf);
				int progress = 0;

				for(VFile v : vf.list()) {
					extractData(v, prog);

					progress += getFileNumber(v);

					prog.accept(progress * 1.0 / num);
				}
			}
		}

		private int getFileNumber(VFile vf) {
			if(vf.getData() != null)
				return 1;

			int result = 0;

			for(VFile v : vf.list()) {
				if(v.getData() != null)
					result++;
				else
					result += getFileNumber(v);
			}

			return result;
		}
	}

	public static final int ver = 50210;
	public static final boolean isBeta = false;
	private static final DecimalFormat df = new DecimalFormat("#.##");

	public static byte FILTER_TYPE = 1;
	public static int autoSaveTime = 0;
	public static final boolean WRITE = !new File("./.idea").exists();
	public static boolean preload = false, trueRun = true, loaded = false, USE_JOGL = false;
	public static boolean light = true, nimbus = false, seconds = false, buttonSound = false;
	public static String author = "";
	public static ImageBuilder<BufferedImage> builder;
	public static boolean announce0510 = false;
	public static AutoSaveTimer ast;

	public static void restartAutoSaveTimer() {
		if (ast != null)
			ast.interrupt();
		ast = autoSaveTime > 0 ? new AutoSaveTimer() : null;
	}

	public static String getTime() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	}

	public static String toSeconds(int in) {
		return df.format(in / 30.0) + "s";
	}

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(MainBCU::noticeErr);
		trueRun = true;
		UserProfile.profile();
		CommonStatic.ctx = new AdminContext();
		CommonStatic.def = new UtilPC.PCItr();

		BCUWriter.logPrepare();
		BCUWriter.logSetup();
		BCUReader.readInfo();

		ImageBuilder.builder = builder = USE_JOGL ? new GLIB() : FIBI.builder;
		BBBuilder.def = USE_JOGL ? new GLBBB() : AWTBBB.INS;

		if (nimbus) {
			if (light) {
				Theme.LIGHT.setTheme();
				Page.BGCOLOR = new Color(255, 255, 255);
			} else {
				Theme.DARK.setTheme();
				Page.BGCOLOR = new Color(40, 40, 40);
			}
		} else {
			Page.BGCOLOR = new Color(255, 255, 255);
		}

		new MainFrame(Data.revVer(MainBCU.ver)).initialize();
		new Timer().start();

		if(!announce0510 && checkOldFileExisting()) {
			Opts.popAgreement("Before migrating v5", "<html><p style=\"width:500px\">This BCU version has completely different code structure from previous version (0-4-10-9), so others data will get reformatted. This process cannot be undone, and it may cause error while reformatting. We recommend you to backup your files (user/replays/res folder) before migrating v5. Agree on this text to continue.</p></html>");
		} else {
			announce0510 = true;
		}

		if (isBeta && !Opts.conf("This is a beta release. Are you sure you want to continue?"))
			System.exit(0);

		MenuBarHandler.initialize();
		BCJSON.check();
		CommonStatic.ctx.initProfile();

		BCUReader.getData$1();
		BattleBox.StageNamePainter.read();
		loaded = true;

		JMenuItem menu = MenuBarHandler.getFileItem("Save All");

		if (menu != null)
			menu.setEnabled(true);

		ast = autoSaveTime > 0 ? new AutoSaveTimer() : null;
		MainFrame.changePanel(new MainPage());
	}

	public static String validate(String str, char replace) {
		char[] chs = new char[] { '.', '/', '\\', ':', '*', '?', '"', '<', '>', '|' };
		for (char c : chs)
			str = str.replace(c, replace);
		return str;
	}

	private static void noticeErr(Thread t, Throwable e) {
		String msg = e.getMessage() + " in " + t.getName();
		Exception exc = (e instanceof Exception) ? (Exception) e : new Exception(msg, e);
		if (CommonStatic.ctx != null)
			CommonStatic.ctx.noticeErr(exc, ErrType.FATAL, msg);
		e.printStackTrace();
	}

	private static boolean checkOldFileExisting() {
		File res = new File(CommonStatic.ctx.getBCUFolder(), "./res");
		File assets = new File(CommonStatic.ctx.getBCUFolder(), "./assets/assets/zip");

		return res.exists() && assets.exists();
	}

	private static class AutoSaveTimer extends Thread {

		public AutoSaveTimer() {
			super();
			start();
		}

		@Override
		public void run() {
			try {
				Thread.sleep(MainBCU.autoSaveTime * 60000L);
				Source.Workspace.autoSave();
				MainBCU.restartAutoSaveTimer();
			} catch (InterruptedException ignored) {
			}
		}
	}
}
