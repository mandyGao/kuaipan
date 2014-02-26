
package cn.kuaipan.android.sdk.exception;

import android.content.res.Resources;

import java.io.Serializable;

public interface IKscError extends ErrorCode, Serializable {

    String getSimpleMessage();

    String getReason(Resources res);

    int getErrorCode();
}
