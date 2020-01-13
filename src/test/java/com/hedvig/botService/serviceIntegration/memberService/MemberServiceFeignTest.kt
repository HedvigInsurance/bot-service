package com.hedvig.botService.serviceIntegration.memberService

import com.hedvig.botService.serviceIntegration.memberService.dto.Address
import com.hedvig.botService.serviceIntegration.memberService.dto.SweAddressRequest
import com.hedvig.botService.serviceIntegration.memberService.dto.SweAddressResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MemberServiceFeignTest {

  @Mock lateinit var serviceClient: MemberServiceClient

    @Test
    fun lookupAddressSWE_whenFeignThrowsException_returnsNull() {

      given(serviceClient.lookupAddressSwe(SweAddressRequest("191212121212", "1337"))).willThrow(RuntimeException(""))

      val sut = MemberServiceFeign(serviceClient)

      val result = sut.lookupAddressSWE("191212121212", "1337")

      assertThat(result).isNull()
    }

  @Test
  fun lookupAddressSWE_whenFound_returnsData() {

    given(serviceClient.lookupAddressSwe(SweAddressRequest("191212121212", "1337"))).willReturn(SweAddressResponse("Tolvan", "Tolvansson", Address("Street", "Stockholm", "12345", "1001", 0)))

    val sut = MemberServiceFeign(serviceClient)

    val result = sut.lookupAddressSWE("191212121212", "1337")

    assertThat(result?.firstName).isEqualTo("Tolvan")
    assertThat(result?.lastName).isEqualTo("Tolvansson")
    assertThat(result?.address?.apartmentNo).isEqualTo("1001")
    assertThat(result?.address?.street).isEqualTo("Street")
    assertThat(result?.address?.city).isEqualTo("Stockholm")
    assertThat(result?.address?.zipCode).isEqualTo("12345")
    assertThat(result?.address?.floor).isEqualTo(0)
  }

  @Test
  fun lookupAddressSWE_whenAddressNotFound_returnsAddressAsNull() {

    given(serviceClient.lookupAddressSwe(SweAddressRequest("191212121212", "1337"))).willReturn(SweAddressResponse("Tolvan", "Tolvansson", null))

    val sut = MemberServiceFeign(serviceClient)

    val result = sut.lookupAddressSWE("191212121212", "1337")

    assertThat(result?.firstName).isEqualTo("Tolvan")
    assertThat(result?.lastName).isEqualTo("Tolvansson")
    assertThat(result?.address).isNull()
  }

}
