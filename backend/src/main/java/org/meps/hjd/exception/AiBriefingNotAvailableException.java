package org.meps.hjd.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class AiBriefingNotAvailableException extends BusinessException {
    public AiBriefingNotAvailableException(String message) {
        super(ErrorCode.AI_BRIEFING_NOT_AVAILABLE, message);
    }
}
