package org.meps.common.llm;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

/** LLM 호출·응답 파싱·검증 실패. 호출부는 이 예외를 잡아 템플릿 폴백으로 응답을 유지한다 */
public class LlmCallFailedException extends BusinessException {

    public LlmCallFailedException(String message) {
        super(ErrorCode.LLM_CALL_FAILED, message);
    }

    public LlmCallFailedException(String message, Throwable cause) {
        super(ErrorCode.LLM_CALL_FAILED, message, cause);
    }
}
