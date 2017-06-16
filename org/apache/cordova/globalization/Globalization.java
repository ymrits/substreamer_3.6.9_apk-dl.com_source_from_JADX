package org.apache.cordova.globalization;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.support.v4.view.PointerIconCompat;
import android.text.format.DateFormat;
import android.text.format.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Globalization extends CordovaPlugin {
    public static final String CURRENCY = "currency";
    public static final String CURRENCYCODE = "currencyCode";
    public static final String DATE = "date";
    public static final String DATESTRING = "dateString";
    public static final String DATETOSTRING = "dateToString";
    public static final String DAYS = "days";
    public static final String FORMATLENGTH = "formatLength";
    public static final String FULL = "full";
    public static final String GETCURRENCYPATTERN = "getCurrencyPattern";
    public static final String GETDATENAMES = "getDateNames";
    public static final String GETDATEPATTERN = "getDatePattern";
    public static final String GETFIRSTDAYOFWEEK = "getFirstDayOfWeek";
    public static final String GETLOCALENAME = "getLocaleName";
    public static final String GETNUMBERPATTERN = "getNumberPattern";
    public static final String GETPREFERREDLANGUAGE = "getPreferredLanguage";
    public static final String ISDAYLIGHTSAVINGSTIME = "isDayLightSavingsTime";
    public static final String ITEM = "item";
    public static final String LONG = "long";
    public static final String MEDIUM = "medium";
    public static final String MONTHS = "months";
    public static final String NARROW = "narrow";
    public static final String NUMBER = "number";
    public static final String NUMBERSTRING = "numberString";
    public static final String NUMBERTOSTRING = "numberToString";
    public static final String OPTIONS = "options";
    public static final String PERCENT = "percent";
    public static final String SELECTOR = "selector";
    public static final String STRINGTODATE = "stringToDate";
    public static final String STRINGTONUMBER = "stringToNumber";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String WIDE = "wide";

    /* renamed from: org.apache.cordova.globalization.Globalization.1 */
    class C02401 implements Comparator<String> {
        final /* synthetic */ Map val$namesMap;

        C02401(Map map) {
            this.val$namesMap = map;
        }

        public int compare(String arg0, String arg1) {
            return ((Integer) this.val$namesMap.get(arg0)).compareTo((Integer) this.val$namesMap.get(arg1));
        }
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        JSONObject obj = new JSONObject();
        try {
            if (action.equals(GETLOCALENAME)) {
                obj = getLocaleName();
            } else if (action.equals(GETPREFERREDLANGUAGE)) {
                obj = getPreferredLanguage();
            } else if (action.equalsIgnoreCase(DATETOSTRING)) {
                obj = getDateToString(data);
            } else if (action.equalsIgnoreCase(STRINGTODATE)) {
                obj = getStringtoDate(data);
            } else if (action.equalsIgnoreCase(GETDATEPATTERN)) {
                obj = getDatePattern(data);
            } else if (action.equalsIgnoreCase(GETDATENAMES)) {
                if (VERSION.SDK_INT < 9) {
                    throw new GlobalizationError(GlobalizationError.UNKNOWN_ERROR);
                }
                obj = getDateNames(data);
            } else if (action.equalsIgnoreCase(ISDAYLIGHTSAVINGSTIME)) {
                obj = getIsDayLightSavingsTime(data);
            } else if (action.equalsIgnoreCase(GETFIRSTDAYOFWEEK)) {
                obj = getFirstDayOfWeek(data);
            } else if (action.equalsIgnoreCase(NUMBERTOSTRING)) {
                obj = getNumberToString(data);
            } else if (action.equalsIgnoreCase(STRINGTONUMBER)) {
                obj = getStringToNumber(data);
            } else if (action.equalsIgnoreCase(GETNUMBERPATTERN)) {
                obj = getNumberPattern(data);
            } else if (!action.equalsIgnoreCase(GETCURRENCYPATTERN)) {
                return false;
            } else {
                obj = getCurrencyPattern(data);
            }
            callbackContext.success(obj);
        } catch (GlobalizationError ge) {
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, ge.toJson()));
        } catch (Exception e) {
            callbackContext.sendPluginResult(new PluginResult(Status.JSON_EXCEPTION));
        }
        return true;
    }

    private String toBcp47Language(Locale loc) {
        String language = loc.getLanguage();
        String region = loc.getCountry();
        String variant = loc.getVariant();
        if (language.equals("no") && region.equals("NO") && variant.equals("NY")) {
            language = "nn";
            region = "NO";
            variant = BuildConfig.FLAVOR;
        }
        if (language.isEmpty() || !language.matches("\\p{Alpha}{2,8}")) {
            language = "und";
        } else if (language.equals("iw")) {
            language = "he";
        } else if (language.equals("in")) {
            language = "id";
        } else if (language.equals("ji")) {
            language = "yi";
        }
        if (!region.matches("\\p{Alpha}{2}|\\p{Digit}{3}")) {
            region = BuildConfig.FLAVOR;
        }
        if (!variant.matches("\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}")) {
            variant = BuildConfig.FLAVOR;
        }
        StringBuilder bcp47Tag = new StringBuilder(language);
        if (!region.isEmpty()) {
            bcp47Tag.append('-').append(region);
        }
        if (!variant.isEmpty()) {
            bcp47Tag.append('-').append(variant);
        }
        return bcp47Tag.toString();
    }

    private JSONObject getLocaleName() throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            obj.put("value", toBcp47Language(Locale.getDefault()));
            return obj;
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.UNKNOWN_ERROR);
        }
    }

    private JSONObject getPreferredLanguage() throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            obj.put("value", toBcp47Language(Locale.getDefault()));
            return obj;
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.UNKNOWN_ERROR);
        }
    }

    private JSONObject getDateToString(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            Date date = new Date(((Long) options.getJSONObject(0).get(DATE)).longValue());
            return obj.put("value", new SimpleDateFormat(getDatePattern(options).getString("pattern")).format(date));
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.FORMATTING_ERROR);
        }
    }

    private JSONObject getStringtoDate(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            Date date = new SimpleDateFormat(getDatePattern(options).getString("pattern")).parse(options.getJSONObject(0).get(DATESTRING).toString());
            Time time = new Time();
            time.set(date.getTime());
            obj.put("year", time.year);
            obj.put("month", time.month);
            obj.put("day", time.monthDay);
            obj.put("hour", time.hour);
            obj.put("minute", time.minute);
            obj.put("second", time.second);
            obj.put("millisecond", Long.valueOf(0));
            return obj;
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.PARSING_ERROR);
        }
    }

    private JSONObject getDatePattern(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            SimpleDateFormat fmtDate = (SimpleDateFormat) DateFormat.getDateFormat(this.cordova.getActivity());
            SimpleDateFormat fmtTime = (SimpleDateFormat) DateFormat.getTimeFormat(this.cordova.getActivity());
            String fmt = fmtDate.toLocalizedPattern() + " " + fmtTime.toLocalizedPattern();
            if (options.getJSONObject(0).has(OPTIONS)) {
                JSONObject innerOptions = options.getJSONObject(0).getJSONObject(OPTIONS);
                if (!innerOptions.isNull(FORMATLENGTH)) {
                    String fmtOpt = innerOptions.getString(FORMATLENGTH);
                    if (fmtOpt.equalsIgnoreCase(MEDIUM)) {
                        fmtDate = (SimpleDateFormat) DateFormat.getMediumDateFormat(this.cordova.getActivity());
                    } else if (fmtOpt.equalsIgnoreCase(LONG) || fmtOpt.equalsIgnoreCase(FULL)) {
                        fmtDate = (SimpleDateFormat) DateFormat.getLongDateFormat(this.cordova.getActivity());
                    }
                }
                fmt = fmtDate.toLocalizedPattern() + " " + fmtTime.toLocalizedPattern();
                if (!innerOptions.isNull(SELECTOR)) {
                    String selOpt = innerOptions.getString(SELECTOR);
                    if (selOpt.equalsIgnoreCase(DATE)) {
                        fmt = fmtDate.toLocalizedPattern();
                    } else if (selOpt.equalsIgnoreCase(TIME)) {
                        fmt = fmtTime.toLocalizedPattern();
                    }
                }
            }
            TimeZone tz = TimeZone.getTimeZone(Time.getCurrentTimezone());
            obj.put("pattern", fmt);
            obj.put("timezone", tz.getDisplayName(tz.inDaylightTime(Calendar.getInstance().getTime()), 0));
            obj.put("utc_offset", tz.getRawOffset() / PointerIconCompat.TYPE_DEFAULT);
            obj.put("dst_offset", tz.getDSTSavings() / PointerIconCompat.TYPE_DEFAULT);
            return obj;
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.PATTERN_ERROR);
        }
    }

    @TargetApi(9)
    private JSONObject getDateNames(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        JSONArray value = new JSONArray();
        List<String> namesList = new ArrayList();
        int type = 0;
        int item = 0;
        try {
            Map<String, Integer> namesMap;
            if (options.getJSONObject(0).length() > 0) {
                if (!((JSONObject) options.getJSONObject(0).get(OPTIONS)).isNull(TYPE) && ((String) ((JSONObject) options.getJSONObject(0).get(OPTIONS)).get(TYPE)).equalsIgnoreCase(NARROW)) {
                    type = 0 + 1;
                }
                if (!((JSONObject) options.getJSONObject(0).get(OPTIONS)).isNull(ITEM) && ((String) ((JSONObject) options.getJSONObject(0).get(OPTIONS)).get(ITEM)).equalsIgnoreCase(DAYS)) {
                    item = 0 + 10;
                }
            }
            int method = item + type;
            if (method == 1) {
                namesMap = Calendar.getInstance().getDisplayNames(2, 1, Locale.getDefault());
            } else if (method == 10) {
                namesMap = Calendar.getInstance().getDisplayNames(7, 2, Locale.getDefault());
            } else if (method == 11) {
                namesMap = Calendar.getInstance().getDisplayNames(7, 1, Locale.getDefault());
            } else {
                namesMap = Calendar.getInstance().getDisplayNames(2, 2, Locale.getDefault());
            }
            for (String name : namesMap.keySet()) {
                namesList.add(name);
            }
            Collections.sort(namesList, new C02401(namesMap));
            for (int i = 0; i < namesList.size(); i++) {
                value.put(namesList.get(i));
            }
            return obj.put("value", value);
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.UNKNOWN_ERROR);
        }
    }

    private JSONObject getIsDayLightSavingsTime(JSONArray options) throws GlobalizationError {
        try {
            return new JSONObject().put("dst", TimeZone.getTimeZone(Time.getCurrentTimezone()).inDaylightTime(new Date(((Long) options.getJSONObject(0).get(DATE)).longValue())));
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.UNKNOWN_ERROR);
        }
    }

    private JSONObject getFirstDayOfWeek(JSONArray options) throws GlobalizationError {
        try {
            return new JSONObject().put("value", Calendar.getInstance(Locale.getDefault()).getFirstDayOfWeek());
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.UNKNOWN_ERROR);
        }
    }

    private JSONObject getNumberToString(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        String value = BuildConfig.FLAVOR;
        try {
            return obj.put("value", getNumberFormatInstance(options).format(options.getJSONObject(0).get(NUMBER)));
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.FORMATTING_ERROR);
        }
    }

    private JSONObject getStringToNumber(JSONArray options) throws GlobalizationError {
        try {
            return new JSONObject().put("value", getNumberFormatInstance(options).parse((String) options.getJSONObject(0).get(NUMBERSTRING)));
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.PARSING_ERROR);
        }
    }

    private JSONObject getNumberPattern(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            DecimalFormat fmt = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
            String symbol = String.valueOf(fmt.getDecimalFormatSymbols().getDecimalSeparator());
            if (options.getJSONObject(0).length() > 0 && !((JSONObject) options.getJSONObject(0).get(OPTIONS)).isNull(TYPE)) {
                String fmtOpt = (String) ((JSONObject) options.getJSONObject(0).get(OPTIONS)).get(TYPE);
                if (fmtOpt.equalsIgnoreCase(CURRENCY)) {
                    fmt = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
                    symbol = fmt.getDecimalFormatSymbols().getCurrencySymbol();
                } else if (fmtOpt.equalsIgnoreCase(PERCENT)) {
                    fmt = (DecimalFormat) DecimalFormat.getPercentInstance(Locale.getDefault());
                    symbol = String.valueOf(fmt.getDecimalFormatSymbols().getPercent());
                }
            }
            obj.put("pattern", fmt.toPattern());
            obj.put("symbol", symbol);
            obj.put("fraction", fmt.getMinimumFractionDigits());
            obj.put("rounding", Integer.valueOf(0));
            obj.put("positive", fmt.getPositivePrefix());
            obj.put("negative", fmt.getNegativePrefix());
            obj.put("decimal", String.valueOf(fmt.getDecimalFormatSymbols().getDecimalSeparator()));
            obj.put("grouping", String.valueOf(fmt.getDecimalFormatSymbols().getGroupingSeparator()));
            return obj;
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.PATTERN_ERROR);
        }
    }

    private JSONObject getCurrencyPattern(JSONArray options) throws GlobalizationError {
        JSONObject obj = new JSONObject();
        try {
            DecimalFormat fmt = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
            Currency currency = Currency.getInstance(options.getJSONObject(0).getString(CURRENCYCODE));
            fmt.setCurrency(currency);
            obj.put("pattern", fmt.toPattern());
            obj.put("code", currency.getCurrencyCode());
            obj.put("fraction", fmt.getMinimumFractionDigits());
            obj.put("rounding", Integer.valueOf(0));
            obj.put("decimal", String.valueOf(fmt.getDecimalFormatSymbols().getDecimalSeparator()));
            obj.put("grouping", String.valueOf(fmt.getDecimalFormatSymbols().getGroupingSeparator()));
            return obj;
        } catch (Exception e) {
            throw new GlobalizationError(GlobalizationError.FORMATTING_ERROR);
        }
    }

    private DecimalFormat getNumberFormatInstance(JSONArray options) throws JSONException {
        DecimalFormat fmt = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        try {
            if (options.getJSONObject(0).length() <= 1 || ((JSONObject) options.getJSONObject(0).get(OPTIONS)).isNull(TYPE)) {
                return fmt;
            }
            String fmtOpt = (String) ((JSONObject) options.getJSONObject(0).get(OPTIONS)).get(TYPE);
            if (fmtOpt.equalsIgnoreCase(CURRENCY)) {
                return (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
            } else if (!fmtOpt.equalsIgnoreCase(PERCENT)) {
                return fmt;
            } else {
                return (DecimalFormat) DecimalFormat.getPercentInstance(Locale.getDefault());
            }
        } catch (JSONException e) {
            return fmt;
        }
    }
}
