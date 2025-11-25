package com.angrypodo.wisp.runtime.parser

import android.net.Uri

interface WispUriParser {
    /**
     * @param uri 수신된 딥링크 Uri
     * @return 백스택을 나타내는 경로 문자열 리스트
     */
    fun parse(uri: Uri): List<String>
}
