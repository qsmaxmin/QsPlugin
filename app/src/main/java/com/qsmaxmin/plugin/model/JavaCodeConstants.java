package com.qsmaxmin.plugin.model;

/**
 * @CreateBy administrator
 * @Date 2020/5/13 12:35
 * @Description
 */
public class JavaCodeConstants {

    public static final String CODE_EXECUTOR_FINDER = "package com.qsmaxmin.ann;\n" +
            "\n" +
            "import com.qsmaxmin.ann.config.PropertiesExecutor;\n" +
            "import com.qsmaxmin.ann.event.EventExecutor;\n" +
            "import com.qsmaxmin.ann.viewbind.ViewAnnotationExecutor;\n" +
            "\n" +
            "/**\n" +
            " * 该代码由QsPlugin动态生成，拒绝外部修改\n" +
            " */\n" +
            "public final class AnnotationExecutorFinder {\n" +
            "\n" +
            "    public static <T> ViewAnnotationExecutor<T> getViewAnnotationExecutor(Class clazz) {\n" +
            "        return create(clazz, \"_QsBind\", true);\n" +
            "    }\n" +
            "\n" +
            "    public static <T> PropertiesExecutor<T> getPropertiesExecutor(Class clazz) {\n" +
            "        return create(clazz, \"_QsConfig\", false);\n" +
            "    }\n" +
            "\n" +
            "    public static <T> EventExecutor<T> getEventExecutor(Class clazz) {\n" +
            "        return create(clazz, \"_QsEvent\", false);\n" +
            "    }\n" +
            "\n" +
            "    @SuppressWarnings(\"unchecked\")\n" +
            "    public static <T> T create(Class clazz, String extraName, boolean supportInnerClass) {\n" +
            "        String className;\n" +
            "        String name = clazz.getName();\n" +
            "        int index_ = name.indexOf('$');\n" +
            "        if (index_ != -1) {\n" +
            "            if (supportInnerClass) {\n" +
            "                int pointIndex = name.lastIndexOf('.');\n" +
            "                String packageName = name.substring(0, pointIndex);\n" +
            "                String simpleName = name.substring(index_ + 1);\n" +
            "                className = packageName + \".\" + simpleName + extraName;\n" +
            "            } else {\n" +
            "                return null;\n" +
            "            }\n" +
            "        } else {\n" +
            "            className = name + extraName;\n" +
            "        }\n" +
            "        try {\n" +
            "            Class<?> myClass = Class.forName(className);\n" +
            "            return (T) myClass.newInstance();\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "            return null;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";


    public static final String CODE_EVENT_SUPER_CLASS = "package com.qsmaxmin.ann.event;\n" +
            "\n" +
            "/**\n" +
            " * @CreateBy administrator\n" +
            " * @Date 2020/5/13 14:34\n" +
            " * @Description\n" +
            " */\n" +
            "public class EventExecutor<T> {\n" +
            "\n" +
            "}\n";


    public static final String CODE_VIEW_BIND_SUPER_CLASS = "package com.qsmaxmin.ann.viewbind;\n" +
            "\n" +
            "import android.os.Bundle;\n" +
            "import android.view.View;\n" +
            "\n" +
            "/**\n" +
            " * @CreateBy qsmaxmin\n" +
            " * @Date 2019/6/6 17:42\n" +
            " * @Description\n" +
            " */\n" +
            "@SuppressWarnings({\"unchecked\"})\n" +
            "public class ViewAnnotationExecutor<T> {\n" +
            "    public void bindView(T target, View view) {\n" +
            "    }\n" +
            "\n" +
            "    public void bindBundle(T target, Bundle bundle) {\n" +
            "    }\n" +
            "\n" +
            "    public final <D extends View> D forceCastView(View view) {\n" +
            "        return (D) view;\n" +
            "    }\n" +
            "\n" +
            "    public final <D> D forceCastObject(Object o) {\n" +
            "        return (D) o;\n" +
            "    }\n" +
            "}\n";

    public static final String CODE_CONFIG_SUPER_CLASS = "package com.qsmaxmin.ann.config;\n" +
            "\n" +
            "import android.content.SharedPreferences;\n" +
            "\n" +
            "import com.google.gson.Gson;\n" +
            "\n" +
            "/**\n" +
            " * @CreateBy qsmaxmin\n" +
            " * @Date 2019/7/22 15:28\n" +
            " * @Description executor 超类\n" +
            " */\n" +
            "@SuppressWarnings({\"unchecked\", \"WeakerAccess\"})\n" +
            "public abstract class PropertiesExecutor<T> {\n" +
            "    private Gson gson;\n" +
            "\n" +
            "    public abstract void bindConfig(T config, SharedPreferences sp);\n" +
            "\n" +
            "    public abstract void commit(T config, SharedPreferences sp);\n" +
            "\n" +
            "    /**\n" +
            "     * base on int\n" +
            "     */\n" +
            "    public final int forceCastInt(Object o) {\n" +
            "        return o == null ? 0 : (int) o;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on int\n" +
            "     */\n" +
            "    public final short forceCastToShort(Object o) {\n" +
            "        if (o == null) return 0;\n" +
            "        int intValue = (int) o;\n" +
            "        return (short) intValue;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on int\n" +
            "     */\n" +
            "    public final byte forceCastToByte(Object o) {\n" +
            "        if (o == null) return 0;\n" +
            "        int intValue = (int) o;\n" +
            "        return (byte) intValue;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on int\n" +
            "     */\n" +
            "    public final char forceCastToChar(Object o) {\n" +
            "        if (o == null) return 0;\n" +
            "        int intValue = (int) o;\n" +
            "        return (char) intValue;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on long\n" +
            "     */\n" +
            "    public final long forceCastToLong(Object o) {\n" +
            "        if (o == null) return 0;\n" +
            "        return (long) o;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on float\n" +
            "     */\n" +
            "    public final float forceCastToFloat(Object o) {\n" +
            "        return o == null ? 0f : (float) o;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on String\n" +
            "     * 历史遗留问题，因为double转float会丢失精度\n" +
            "     * 所以在以后的版本里同String保存double数据，此处做了兼容处理\n" +
            "     */\n" +
            "    public final double forceCastToDouble(Object o) {\n" +
            "        if (o == null) return 0;\n" +
            "        if (o instanceof String) {\n" +
            "            String stringValue = (String) o;\n" +
            "            return Double.parseDouble(stringValue);\n" +
            "        } else {\n" +
            "            float floatValue = (float) o;\n" +
            "            return (double) floatValue;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on boolean\n" +
            "     */\n" +
            "    public final boolean forceCastToBoolean(Object o) {\n" +
            "        return o != null && (boolean) o;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * base on String\n" +
            "     */\n" +
            "    public final <D> D forceCastObject(Object object) {\n" +
            "        return object == null ? null : (D) object;\n" +
            "    }\n" +
            "\n" +
            "    public final <D> D jsonStringToObject(Object o, Class<D> clazzOfD) {\n" +
            "        if (o instanceof String) {\n" +
            "            if (gson == null) gson = new Gson();\n" +
            "            return gson.fromJson((String) o, clazzOfD);\n" +
            "        }\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    public final String objectToJsonString(Object o, Class clazz) {\n" +
            "        if (o != null) {\n" +
            "            if (gson == null) gson = new Gson();\n" +
            "            return gson.toJson(o, clazz);\n" +
            "        }\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    public String doubleCastToString(Double d) {\n" +
            "        double doubleValue = d == null ? 0 : d;\n" +
            "        return String.valueOf(doubleValue);\n" +
            "    }\n" +
            "}";
}
