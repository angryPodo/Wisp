package com.angrypodo.wisp.runtime.spi

/**
 * KSP가 생성하는 팩토리 클래스들이 구현할 인터페이스입니다.
 * 다른 모듈에서 생성된 코드가 접근해야 하므로 public으로 선언합니다.
 */
interface RouteFactory {
    fun create(params: Map<String, String>): Any
}
