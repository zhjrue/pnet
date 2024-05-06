package cool.ldr.pnet.api;

import android.content.Context;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cool.ldr.pnet.R;
import cool.ldr.pnet.entity.Kinetics;
import cool.ldr.pnet.entity.Krlh;
import cool.ldr.pnet.entity.Reference;
import cool.ldr.pnet.utils.Constant;

public class data {


    public static boolean saveList(String fileName, List<?> list) {
        final String filePath = PathUtils.getInternalAppFilesPath() + "/json/" + fileName;

        try {
            FileIOUtils.writeFileFromString(filePath, GsonUtils.toJson(list));
            return true;
        } catch (Exception e) {
            LogUtils.e(e);
            e.printStackTrace();
            return false;
        }
    }

    public static <T> List<T> getList(String fileName, Type classOfT) {
        final String filePath = PathUtils.getInternalAppFilesPath() + "/json/" + fileName;

        List<T> list = new ArrayList<T>();
        try {
            String s = FileIOUtils.readFile2String(filePath);
//            LogUtils.i(filePath + "\n" + s);
            list = new Gson().fromJson(s, GsonUtils.getListType(classOfT));
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
        if (list == null) list = new ArrayList<>();

        return list;

    }


    public static <T> List<T> getRawList(Context context, int fileRaw, Type classOfT) {
        List<T> list = new ArrayList<T>();
        try {
            InputStream inputStream = context.getResources().openRawResource(fileRaw);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, length);
            }
            outStream.close();
            inputStream.close();

            list = new Gson().fromJson(outStream.toString(), GsonUtils.getListType(classOfT));
        } catch (Exception e) {
            LogUtils.e(e.toString());
        }
        if (list == null) list = new ArrayList<>();

        return list;
    }

    public static List<Kinetics> filterData(String province, String area, List<Kinetics> kineticsList) {

        String s = "";
        if (area.contains("（") && area.contains("）")) {
            s = area.split("（")[1].split("）")[0];
        }
        s = s.toLowerCase();
        LogUtils.d(province, area, s, "材料类型".equals(province), "酶类型".equals(province));

        List<Kinetics> kineticsList1 = new ArrayList<>();

        if ("材料类型".equals(province)) {
            LogUtils.d(s);

            for (Kinetics kinetics : kineticsList) {
                if (Objects.equals(kinetics.getMaterial_type().toLowerCase(), s)) {
                    kineticsList1.add(kinetics);
                }
            }
        } else if ("酶类型".equals(province)) {
            if (s.contains("，")) {
                s = s.split("，")[1];
            }
            LogUtils.d(s);

            for (Kinetics kinetics : kineticsList) {
                if (Objects.equals(s, kinetics.getEnzyme_type().toLowerCase())) {
                    kineticsList1.add(kinetics);
                }
            }
        }
        LogUtils.d(kineticsList1.size());
        return kineticsList1;
    }

    public static Krlh getKrlh() {
        List<Kinetics> rawList0 = getRawList(Utils.getApp(), R.raw.kinetics, Kinetics.class);
        List<Reference> rawList1 = getRawList(Utils.getApp(), R.raw.references, Reference.class);
        for (Kinetics kinetics : rawList0) {
            for (Reference reference : rawList1) {
                if (kinetics.getRef().equals(reference.getRef())) {
                    kinetics.setMaterial_type(reference.getMaterial_type());
                    kinetics.setDoi(reference.getDOI());
                    kinetics.setTitle(reference.getTitle());
                }
            }
        }

        List<String> loveIds = data.getList(Constant.loveFileName, String.class);
        List<String> historyIds = data.getList(Constant.historyFileName, String.class);

        return new Krlh(rawList0, rawList1, loveIds, historyIds);
    }

}
