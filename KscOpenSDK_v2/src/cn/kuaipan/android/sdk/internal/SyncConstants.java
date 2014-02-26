
package cn.kuaipan.android.sdk.internal;

public interface SyncConstants {
    // String SYNC_HOST = Constants.DEBUG_INTERNAL ? "192.168.135.66:9101"
    // : "contactapi.kuaipan.cn";
    String SYNC_HOST = "192.168.135.66:9101";
    String SYNC_URL_BASE = Constants.HTTP + SYNC_HOST + "/open";

    String URI_ADD_SOURCE = SYNC_URL_BASE + "/addSource";
    String URI_GET_SOURCE = SYNC_URL_BASE + "/getSource";

    String URI_ADD_CALLLOGS = SYNC_URL_BASE + "/batchAddCallLogs";
    String URI_GET_MISSED_CALLLOGS = SYNC_URL_BASE + "/getMissedCallLogs";
    String URI_GET_INCOMING_CALLLOGS = SYNC_URL_BASE + "/getIncomingCallLogs";
    String URI_GET_OUTGOING_CALLLOGS = SYNC_URL_BASE + "/getOutgoingCallLogs";
    String URI_GET_CALLLOG_TOTAL_COUNT = SYNC_URL_BASE
            + "/getCallLogTotalCount";
    // String URI_GET_CALLLOG_DIFF_COUNT = SYNC_URL_BASE +
    // "/getCallLogNewCount";

    String URI_ADD_SMS = SYNC_URL_BASE + "/batchAddSms";
    String URI_GET_THREADS = SYNC_URL_BASE + "/getThreads";
    String URI_GET_SMS = SYNC_URL_BASE + "/getSmses";
    String URI_GET_SMS_TOTAL_COUNT = SYNC_URL_BASE + "/getSmsTotalCount";
    // String URI_GET_SMS_DIFF_COUNT = SYNC_URL_BASE + "/getSmsNewCount";

    String URI_GET_UPDATED_CONTACTS = SYNC_URL_BASE + "/syncContacts";
    String URI_GET_DELETED_CONTACTS = SYNC_URL_BASE + "/getDeletedContacts";
    String URI_GET_UPDATED_CONTACTS_COUNT = SYNC_URL_BASE
            + "/syncContactsCount";
    String URI_GET_DELETED_CONTACTS_COUNT = SYNC_URL_BASE
            + "/getDeletedContactsCount";
    String URI_GET_CONTACTS_COUNT_AT_TIME = SYNC_URL_BASE
            + "/getContactsCountAtTime";
    String URI_GET_CONTACTS_AT_TIME = SYNC_URL_BASE + "/getContactsAtTime";
    String URI_GET_RECORDS = SYNC_URL_BASE + "/getRecords";

    String URI_RECOVERY_TO_RECORD = SYNC_URL_BASE + "/recoveryToRecord";
    String URI_RECOVERY_DELETED_CONTACTS = SYNC_URL_BASE
            + "/recoveryDeletedContacts";
    String URI_UPLOAD_CONTACTS = SYNC_URL_BASE + "/multiContactsOperation";
    String URI_DELETE_ALL_CONTACTS = SYNC_URL_BASE + "/deleteAllContacts";
    String URI_ADD_RECORD = SYNC_URL_BASE + "/addRecord";
    String URI_GET_RECORD_DETAILS = SYNC_URL_BASE + "/getRecordDetails";

    String PARAM_PHONE_NUM = "phone_num";
    String PARAM_PLATFORM = "platform";
    String PARAM_DEVICE = "device";
    String PARAM_DETAILS = "details";
    String PARAM_IMEI = "imei";
    String PARAM_IMSI = "imsi";
    String PARAM_LIMIT = "limit";
    String PARAM_TIMESTAMP = "timestamp";
    String PARAM_BEGIN_ID = "begin_id";
    String PARAM_PREVIOUS_ID = "previous_id";
    String PARAM_SID = "sid";
    String PARAM_TID = "tid";

    String PARAM_RID = "rid";
    String PARAM_TARGET_RID = "target_rid";
    String PARAM_CONTACT_IDS = "contact_ids";

    String PARAM_CALLLOGS = "ignore-calllogs";
    String PARAM_THREADS = "ignore-threads";
    String PARAM_CONTACTS = "contacts";
    String PARAM_OPREATIONS = "operations";

    String DATA_SID = "sid";

    String MAX_PAGE_COUNT = "50";

}
