package page.info.edit;

import common.pack.PackData.UserPack;
import common.pack.UserProfile;
import common.util.stage.MapColc;
import common.util.stage.SCDef;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.util.stage.info.CustomStageInfo;
import common.util.unit.AbEnemy;
import common.util.unit.EneRand;
import common.util.unit.Enemy;
import custom.Analyser;
import custom.Fio.FileChooserService;
import custom.ReflectUtils;

import java.awt.event.ActionEvent;

import custom.StageExporter;
import main.Opts;
import page.JBTN;
import page.MainLocale;
import page.Page;
import page.battle.BattleSetupPage;
import page.battle.StRecdPage;
import page.info.filter.EnemyFindPage;
import page.support.AnimLCR;
import page.support.RLFIM;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StageEditPage extends Page {

    private static final long serialVersionUID = 1L;

    public static void redefine() {
        StageEditTable.redefine();
        LimitTable.redefine();
        SCGroupEditTable.redefine();
    }

    private final JBTN back = new JBTN(0, "back");
    private final JBTN strt = new JBTN(0, "start");
    private final JBTN veif = new JBTN(0, "veif");
    private final JBTN cpsm = new JBTN(0, "cpsm");
    private final JBTN cpst = new JBTN(0, "cpst");
    private final JBTN ptsm = new JBTN(0, "ptsm");
    private final JBTN ptst = new JBTN(0, "ptst");
    private final JBTN rmsm = new JBTN(0, "rmsm");
    private final JBTN rmst = new JBTN(0, "rmst");
    private final JBTN recd = new JBTN(0, "replay");
    private final JBTN elim = new JBTN(0, "limit");
    private final StageEditTable jt;
    private final JScrollPane jspjt;
    private final RLFIM<StageMap> jlsm = new RLFIM<>(() -> this.changing = true, () -> changing = false,
                                                     this::finishRemoving, this::setAA, StageMap::new);
    private final JScrollPane jspsm = new JScrollPane(jlsm);
    private final RLFIM<Stage> jlst = new RLFIM<>(() -> this.changing = true, () -> changing = false,
                                                  this::finishRemoving, this::setAB, Stage::new);
    private final JScrollPane jspst = new JScrollPane(jlst);
    private final JList<StageMap> lpsm = new JList<>(Stage.CLIPMC.maps.toArray());
    private final JScrollPane jlpsm = new JScrollPane(lpsm);
    private final JList<Stage> lpst = new JList<>();
    private final JScrollPane jlpst = new JScrollPane(lpst);
    private final JBTN adds = new JBTN(0, "add");
    private final JBTN rems = new JBTN(0, "rem");
    private final JBTN addl = new JBTN(0, "addl");
    private final JBTN reml = new JBTN(0, "reml");
    private final JBTN advs = new JBTN(0, "advance");
    private final JList<AbEnemy> jle = new JList<>();
    private final JScrollPane jspe = new JScrollPane(jle);

    private final HeadEditTable info;

    private final MapColc mc;
    private final String pack;

    private final EnemyFindPage efp;

    private boolean changing = false;
    private Stage stage;

    public StageEditPage(Page p, MapColc map, UserPack pac) {
        super(p);
        mc = map;
        pack = pac.desc.id;
        jt = new StageEditTable(this, pac);
        jspjt = new JScrollPane(jt);
        info = new HeadEditTable(this, pac);
        jlsm.setListData(mc, mc.maps);
        jle.setListData(UserProfile.getAll(pack, Enemy.class).toArray(new Enemy[0]));
        efp = new EnemyFindPage(getThis(), pac.desc.id, pac.desc.dependency.toArray(new String[0]));
        ini();
    }

    @Override
    protected JButton getBackButton() {
        return back;
    }

    @Override
    protected void mouseClicked(MouseEvent e) {
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if (e.getSource() == jt && (e.getModifiers() & modifier) == 0)
            jt.clicked(e.getPoint(), e.getButton());
    }

    @Override
    protected void renew() {
        info.renew();

        Vector<AbEnemy> v = new Vector<>();

        if (efp != null && efp.getList() != null)
            v.addAll(efp.getList());

        UserPack p = UserProfile.getUserPack(pack);

        if (p != null) {
            ArrayList<EneRand> rands = new ArrayList<>(p.randEnemies.getList());

            for (String str : p.desc.dependency) {
                UserPack parent = UserProfile.getUserPack(str);

                if (parent != null) {
                    rands.addAll(parent.randEnemies.getList());
                }
            }

            v.addAll(rands);
        }

        jle.setListData(v);
    }

    @Override
    protected synchronized void resized(int x, int y) {
        setBounds(0, 0, x, y);
        set(back, x, y, 0, 0, 200, 50);
        set(info, x, y, 900, 50, 1400, 300);
        set(addl, x, y, 900, 400, 200, 50);
        set(reml, x, y, 1100, 400, 200, 50);
        set(elim, x, y, 1600, 400, 200, 50);
        set(recd, x, y, 1850, 400, 200, 50);
        set(advs, x, y, 2100, 400, 200, 50);
        set(jspjt, x, y, 900, 450, 1400, 850);

        set(jspsm, x, y, 0, 50, 300, 800);
        set(cpsm, x, y, 0, 850, 300, 50);
        set(ptsm, x, y, 0, 900, 300, 50);
        set(rmsm, x, y, 0, 950, 300, 50);
        set(jlpsm, x, y, 0, 1000, 300, 300);

        set(strt, x, y, 300, 0, 300, 50);
        set(adds, x, y, 300, 50, 150, 50);
        set(rems, x, y, 450, 50, 150, 50);
        set(jspst, x, y, 300, 100, 300, 750);
        set(cpst, x, y, 300, 850, 300, 50);
        set(ptst, x, y, 300, 900, 300, 50);
        set(rmst, x, y, 300, 950, 300, 50);
        set(jlpst, x, y, 300, 1000, 300, 300);

        set(veif, x, y, 600, 0, 300, 50);
        set(jspe, x, y, 600, 50, 300, 1250);
        setExtra(x, y);
        jt.setRowHeight(size(x, y, 50));
    }

    private void addListeners$0() {

        back.setLnr(x -> changePanel(getFront()));

        strt.setLnr(x -> changePanel(new BattleSetupPage(getThis(), stage, 1)));

        advs.setLnr(x -> changePanel(new AdvStEditPage(getThis(), stage)));

        recd.setLnr(x -> changePanel(new StRecdPage(getThis(), stage, true)));

        elim.setLnr(x -> changePanel(new LimitEditPage(getThis(), stage)));

        addl.addActionListener(arg0 -> {
            int ind = jt.addLine(jle.getSelectedValue());
            setData(stage);
            if (ind < 0)
                jt.clearSelection();
            else
                jt.addRowSelectionInterval(ind, ind);
        });

        reml.addActionListener(arg0 -> {
            int ind = jt.remLine();
            setData(stage);
            if (ind < 0)
                jt.clearSelection();
            else
                jt.addRowSelectionInterval(ind, ind);
        });

        veif.setLnr(x -> changePanel(efp));

    }

    private void addListeners$1() {

        jlsm.addListSelectionListener(arg0 -> {
            if (changing || arg0.getValueIsAdjusting())
                return;
            changing = true;
            setAA(jlsm.getSelectedValue());
            changing = false;
        });

        jlst.addListSelectionListener(arg0 -> {
            if (changing || arg0.getValueIsAdjusting())
                return;
            changing = true;
            setAB(jlst.getSelectedValue());
            changing = false;
        });

        lpsm.addListSelectionListener(arg0 -> {
            if (changing || arg0.getValueIsAdjusting())
                return;
            changing = true;
            setBA(lpsm.getSelectedValue());
            changing = false;
        });

        lpst.addListSelectionListener(arg0 -> {
            if (changing || arg0.getValueIsAdjusting())
                return;
            changing = true;
            setBB(lpst.getSelectedValue());
            changing = false;
        });

    }

    private void addListeners$2() {

        cpsm.addActionListener(arg0 -> {
            StageMap sm = jlsm.getSelectedValue();
            MapColc col = Stage.CLIPMC;
            StageMap copy = sm.copy(col);
            col.maps.add(copy);
            changing = true;
            lpsm.setListData(col.maps.toArray());
            lpsm.setSelectedValue(copy, true);
            setBA(copy);
            changing = false;
        });

        cpst.addActionListener(arg0 -> {
            Stage copy = stage.copy(Stage.CLIPSM);
            Stage.CLIPSM.add(copy);
            changing = true;
            lpst.setListData(Stage.CLIPSM.list.toArray());
            lpst.setSelectedValue(copy, true);
            lpsm.setSelectedIndex(0);
            setBB(copy);
            changing = false;
        });

        ptsm.addActionListener(arg0 -> {
            StageMap sm = lpsm.getSelectedValue();
            StageMap ni = sm.copy(mc);
            mc.maps.add(ni);
            changing = true;
            jlsm.setListData(mc, mc.maps);
            jlsm.setSelectedValue(ni, true);
            setBA(ni);
            changing = false;
        });

        ptst.addActionListener(arg0 -> {
            StageMap sm = jlsm.getSelectedValue();
            stage = lpst.getSelectedValue().copy(sm);
            sm.add(stage);
            changing = true;
            jlst.setListData(sm, sm.list);
            jlst.setSelectedValue(stage, true);
            setBB(stage);
            changing = false;
        });

        rmsm.addActionListener(arg0 -> {
            int ind = lpsm.getSelectedIndex();
            MapColc col = Stage.CLIPMC;
            col.maps.remove(lpsm.getSelectedValue());
            changing = true;
            lpsm.setListData(col.maps.toArray());
            lpsm.setSelectedIndex(ind - 1);
            setBA(lpsm.getSelectedValue());
            changing = false;
        });

        rmst.addActionListener(arg0 -> {
            StageMap sm = lpsm.getSelectedValue();
            Stage st = lpst.getSelectedValue();
            int ind = lpst.getSelectedIndex();
            sm.list.remove(st);
            changing = true;
            lpst.setListData(sm.list.toArray());
            lpst.setSelectedIndex(ind - 1);
            setBB(lpst.getSelectedValue());
            changing = false;
        });

        adds.setLnr(jlst::addItem);

        rems.setLnr(jlst::deleteItem);

    }

    private void checkPtsm() {
        StageMap sm = lpsm.getSelectedValue();
        if (sm == null) {
            ptsm.setEnabled(false);
            return;
        }
        Set<String> set = new TreeSet<>();
        for (Stage st : sm.list)
            set.addAll(st.isSuitable(pack));
        ptsm.setEnabled(set.size() == 0);
        if (set.size() > 0)
            ptsm.setToolTipText("requires: " + set);

    }

    private void checkPtst() {
        Stage st = lpst.getSelectedValue();
        StageMap sm = jlsm.getSelectedValue();
        if (st == null || sm == null)
            ptst.setEnabled(false);
        else {
            Set<String> set = st.isSuitable(pack);
            ptst.setEnabled(set.size() == 0);
            if (set.size() > 0)
                ptst.setToolTipText("requires: " + set);
        }
        rmst.setEnabled(st != null);
    }

    private void ini() {
        add(back);
        add(veif);
        add(adds);
        add(rems);
        add(jspjt);
        add(info);
        add(strt);
        add(jspsm);
        add(jspst);
        add(addl);
        add(reml);
        add(jspe);
        add(cpsm);
        add(cpst);
        add(ptsm);
        add(ptst);
        add(rmsm);
        add(rmst);
        add(jlpsm);
        add(jlpst);
        add(recd);
        add(advs);
        add(elim);
        setAA(null);
        setBA(null);
        jle.setCellRenderer(new AnimLCR());
        addListeners$0();
        addListeners$1();
        addListeners$2();
        addExtra();
    }

    private void setAA(StageMap sm) {
        if (sm == null) {
            jlst.setListData(null, null);
            setAB(null);
            cpsm.setEnabled(false);
            ptst.setEnabled(false);
            adds.setEnabled(false);
            return;
        }
        jlst.setListData(sm, sm.list);
        if (sm.list.size() == 0) {
            jlst.clearSelection();
            cpsm.setEnabled(false);
            adds.setEnabled(true);
            checkPtst();
            setAB(null);
            return;
        }
        jlst.setSelectedIndex(0);
        cpsm.setEnabled(true);
        adds.setEnabled(true);
        checkPtst();
        setAB(sm.list.getList().get(0));
    }

    private void finishRemoving(Object obj) {
        if (obj instanceof StageMap) {
            StageMap stm = (StageMap) obj;
            for (Stage s : stm.list)
                if (s.info != null)
                    ((CustomStageInfo) s.info).destroy();
            for (Stage s : stm.list)
                for (CustomStageInfo si : ((MapColc.PackMapColc) mc).si)
                    si.remove(s);
        } else {
            Stage st = (Stage) obj;
            if (st.info != null)
                ((CustomStageInfo) st.info).destroy();
            for (CustomStageInfo si : ((MapColc.PackMapColc) mc).si)
                si.remove(st);
        }
    }

    private void setAB(Stage st) {
        if (st == null) {
            setData(lpst.getSelectedValue());
            cpst.setEnabled(false);
            rems.setEnabled(false);
            return;
        }
        cpst.setEnabled(true);
        rems.setEnabled(true);
        lpst.clearSelection();
        checkPtst();
        setData(st);
    }

    private void setBA(StageMap sm) {
        if (sm == null) {
            lpst.setListData(new Stage[0]);
            ptsm.setEnabled(false);
            rmsm.setEnabled(false);
            setBB(null);
            return;
        }
        lpst.setListData(sm.list.toArray());
        rmsm.setEnabled(sm != Stage.CLIPSM);
        if (sm.list.size() == 0) {
            lpst.clearSelection();
            ptsm.setEnabled(false);
            setBB(null);
            return;
        }
        lpst.setSelectedIndex(0);
        setBB(sm.list.getList().get(0));
        checkPtsm();

    }

    private void setBB(Stage st) {
        if (st == null) {
            setData(jlst.getSelectedValue());
            ptst.setEnabled(false);
            rmst.setEnabled(false);
            return;
        }
        cpst.setEnabled(false);
        checkPtst();
        jlst.clearSelection();
        setData(st);
    }

    private void setData(Stage st) {
        stage = st;
        info.setData(st);
        jt.setData(st);
        strt.setEnabled(st != null);
        recd.setEnabled(st != null);
        advs.setEnabled(st != null);
        elim.setEnabled(st != null && !(st.getCont().getCont() instanceof MapColc.ClipMapColc));
        jspjt.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        resized();
    }

    /* modify part begin */

    private final JBTN importBTN = new JBTN(0, "importStage");
    private final JBTN exportBTN = new JBTN(0, "exportStage");

    @SuppressWarnings("unchecked")
    private void handleStageData(Map<String, Object> stageData) throws IllegalAccessException {
        // handle enemy
        getStrategy((ArrayList<List<Integer>>) stageData.get("enemy"));

        // handle stage config
        ReflectUtils.invokeField(info, stageData, info::input);

        // finish all, setData
        setData(stage);
    }

    /**
     * this method can only accept one file as stage.
     */
    private void selectSingleStage(ActionEvent arg0) {
        if (stage == null) {
            Opts.pop("please select a stage first!", "Tip");
            return;
        }

        File file = FileChooserService.getFile();

        if (file != null) {
            System.out.println("select file:" + file.getAbsolutePath());

            try {
                // get Data
                Map<String, Object> stageData = Analyser.getStageBaseAndEnemyData(file);
                handleStageData(stageData);

            } catch (Exception e) {
                e.printStackTrace();
                Opts.pop("the selected file is unfitted: " + e.getMessage(), "Error");
            }
        }
    }

    /**
     * this method can accept several files as stages and add them to StageMap <br>
     * usage:
     * <pre>
     * put these files in directory which path = ./raw/
     * MapStageDataNA_043.csv
     * stageRNA043_00.csv
     * stageRNA043_01.csv
     * stageRNA043_02.csv
     * stageRNA043_03.csv
     * stageRNA043_04.csv
     * stageRNA043_05.csv
     * </pre>
     */
    private void selectStages(ActionEvent arg0) {
        String root = "./raw/";
        File file = new File(root);
        String[] raw_files_list;
        if (file.exists()) {
            raw_files_list = file.list();
            assert raw_files_list != null;
        } else {
            selectSingleStage(arg0);
            return;
        }

        if (jlst == null || jlst.ic == null) {
            Opts.pop(MainLocale.getLoc(MainLocale.PAGE, "tip-import-stageMap1"), "Tip");
            return;
        }

        /*-----------------------------------------------------*/
        ArrayList<File> musicFiles = new ArrayList<>();

        // get music file
        for (String each : raw_files_list) {
            if (each.startsWith("MapStageData")) {
                musicFiles.add(new File(root + each));
            }
        }

        if (musicFiles.isEmpty()) {
            if (Opts.conf(MainLocale.getLoc(MainLocale.PAGE, "tip-import-stageMap2"))) {
                selectSingleStage(arg0);
            }
            return;
        }

        // get stage file

        musicFiles.forEach((musicFile) -> {
            // MapStageDataNA_042.csv
            String id = musicFile.getName().split("_")[1].substring(0, 3);
            System.out.println("id=" + id);
            // stageRNA042_00
            List<File> stageFiles = Arrays.stream(raw_files_list)
                                          .filter(s -> s.startsWith("stageRNA" + id))
                                          .map("./raw/"::concat)
                                          .map(File::new)
                                          .collect(Collectors.toList());

            Map<Integer, int[]> stage_music_map = Analyser.getStageMusicData(musicFile);

            try {
                for (int i = 0; i < stageFiles.size(); i++) {
                    File stageFile_i = stageFiles.get(i);

                    Map<String, Object> stageData_i = Analyser.getStageBaseAndEnemyData(stageFile_i);

                    int[] musicData_i = stage_music_map.get(i);
                    stageData_i.put("name", "csv_stage_" + id + "-" + i);
                    stageData_i.put("jm0", musicData_i[0]);
                    stageData_i.put("jmh", musicData_i[1]);
                    stageData_i.put("jm1", musicData_i[2]);

                    // target stage
                    jlst.addItem(jlsm.getSelectedValue());

                    handleStageData(stageData_i);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Opts.pop(MainLocale.getLoc(MainLocale.PAGE, "tip-popErr-prefix") + e.getMessage(), "Error");
            }

        });
    }

    private void getStrategy(ArrayList<List<Integer>> enemy) {

        int selectedRowIndex = jt.getSelectedRow();
        if (selectedRowIndex == -1) {
            // didn't select a line of table
            strategy1(enemy);
        } else {
            // select a line of table
            strategy2(enemy, selectedRowIndex);
        }
    }

    /**
     * cover the stage
     */
    private void strategy1(ArrayList<List<Integer>> enemy) {
        if (enemy != null) {
            ArrayList<SCDef.Line> lines = new ArrayList<>(enemy.size());

            enemy.forEach((eachEnemy) -> {
                int index = eachEnemy.get(0) - 2;
                if (index >= jle.getModel().getSize()) {
                    // which means it's a new BC enemy that BCU haven't added.
                    index = 20;
                }
                AbEnemy abEnemy = jle.getModel().getElementAt(index);
                this.addLine(abEnemy, Analyser.ArrayIntegerToInt(eachEnemy, 1), lines);
            });

            jt.stage.datas = Analyser.toReverseArrays(lines, new SCDef.Line[0]);
        }
    }

    /**
     * append to the stage
     */
    private void strategy2(ArrayList<List<Integer>> enemy, int selectRowIndex) {
        if (enemy != null) {
            SCDef.Line[] data = jt.stage.datas;
            ArrayList<SCDef.Line> lines = new ArrayList<>(enemy.size() + data.length);
            int i;
            int splitIndex = data.length - selectRowIndex - 1;

            for (i = data.length - 1; i >= splitIndex; i--) {
                lines.add(data[i]);
            }

            enemy.forEach((List<Integer> eachEnemy) -> this.addLine(jle.getModel().getElementAt(eachEnemy.get(0) - 2),
                                                                    Analyser.ArrayIntegerToInt(eachEnemy, 1), lines));

            for (i = 0; i < splitIndex; i++) {
                lines.add(data[i]);
            }

            jt.stage.datas = Analyser.toReverseArrays(lines, new SCDef.Line[0]);
        }
    }

    public synchronized void addLine(AbEnemy enemy, int[] enemyData, ArrayList<SCDef.Line> lines) {
        if (stage == null) return;
        SCDef.Line line = new SCDef.Line();
        line.enemy = enemy == null ? null : enemy.getID();
        line.number = enemyData[0];
        line.castle_0 = enemyData[4];
        line.layer_0 = enemyData[5];
        line.layer_1 = enemyData[6];
        line.multiple = enemyData[8];
        line.mult_atk = enemyData[8];
        line.boss = enemyData[7];
        line.spawn_0 = enemyData[1];
        line.spawn_1 = enemyData[1];
        line.respawn_0 = enemyData[2];
        line.respawn_1 = enemyData[2];
        lines.add(line);
    }

    private void setExtra(int x, int y) {
        // set(reml, x, y, 1100, 400, 200, 50);
        // set(importBTN, x, y, 1300, 400, 200, 50);
        set(exportBTN, x, y, 1300, 400, 200, 50);
    }

    private void addExtra() {
        add(importBTN);
        importBTN.addActionListener(this::selectStages);
        add(exportBTN);
        exportBTN.addActionListener(this::exportStage);
    }

    private void exportStage(ActionEvent event) {
        // {
        //      {"jcas"},
        //      {"jlen", "jhea", "", "", "jbg", "jmax"},
        //      {"enemyIndex", "number", "start/2", "respawn1", "respawn2", "BHPercent",
        //       "layer1", "layer2", "isBoss", "multiple"}
        // };

        StageExporter exporter = new StageExporter();

        // line 1
        // 敌城图片,0,0,0,0,0
        Object[] line1 = new Object[]{info.jcas, 0, 0, 0, 0, 0};

        // line 2
        // 第二行：关卡长度，敌城体力，最小生成，最大生成，背景ID，敌上限，0,0,0,0,
        Object[] line2 = new Object[]{
                info.jlen,
                info.jhea,
                1,
                60,
                info.jbg,
                info.jmax,
                0, 0, 0, 0
        };

        // line3 - enemy line
        // 第三行：敌人ID，数量，初生成，最小再生成，最大再生成，城联动，最小层数，最大层数，是否为boos，倍率,
        SCDef.Line[] enemyLines = jt.stage.datas;
        List<Object[]> line3List = Arrays
                .stream(enemyLines)
                .map(line -> {
                    // check seperated, null data will be ignored
                    boolean seperated = line.mult_atk == line.multiple;
                    // 11
                    Integer mult_atk_holder = seperated ? null : 0;
                    // 12
                    Integer mult_atk = seperated ? null : line.mult_atk;
                    // 13
                    Integer negativeFlag = line.spawn_0 < 0 ? 1 : null;

                    // if 13 is 1, 11 and 12 should be 0 as placeholder
                    if (negativeFlag != null && mult_atk_holder == null) {
                        mult_atk_holder = 0;
                        mult_atk = 0;
                    }

                    return new Object[]{
                            line.enemy.id + 2,
                            line.number,
                            line.spawn_0 / 2, // 初生成
                            line.respawn_0 / 2, // 最小再生成，最大再生成
                            line.respawn_1 / 2, // 最小再生成，最大再生成
                            line.castle_0, // 城联动
                            line.layer_0,
                            line.layer_1,
                            line.boss,
                            line.multiple,
                            mult_atk_holder,
                            mult_atk,
                            negativeFlag
                            };
                }).collect(Collectors.toList());
        Collections.reverse(line3List);
        exporter.addLine1(line1);
        exporter.addLine2(line2);
        exporter.addLine3List(line3List);
        exporter.toFile();
    }

    /* modify part end */

}
