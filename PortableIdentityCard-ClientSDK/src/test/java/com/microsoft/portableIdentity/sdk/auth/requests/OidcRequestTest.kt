package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import org.junit.Test

class OidcRequestTest {

    val token = "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQXktV0VjbmlsVkVudS1CZlFpbTBxaE9IdEdRYlUxTmxrY3h5YkU2UVZRQlE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SjBlWEJsSWpvaVkzSmxZWFJsSWl3aWMzVm1abWw0UkdGMFlTSTZJbVY1U25kWldGSnFZVVZTYUdSSFJrbFpXRTV2U1dwdmFWSlhiRVJZTVVZd1VXMTRWRTB6UlhSVlJHeHFZakJ3VG1WSVdsQk5NR3hZWW5wa2IyRkZTakJYVjFwelQxWktSRmRJUW1oU1JHZzFWRlpPV1dSNVNYTkpia3BzV1RJNU1scFlTalZUTWxZMVNXcHdOMGx0ZERCbFUwazJTV3RXUkVscGQybFpNMG95U1dwdmFXTXlWbXBqUkVreFRtMXplRWxwZDJsbFEwazJTV3hzY2s1VlJtdE5iRWt6WWtWV01GWkZZelJUVmtwYVkzcEtWVTR5ZHpGVlJXaExUMWhrUkdSNlVrMVdWMnQ1WTJzMU1WWkhNVmhhTW5OcFRFTktOVWxxYjJsV1IxSTJVbXRrVlZsV1RtWlhiVkpEV2xjeFJtTldSakZaZWs1NlVqRmthVkl4WkZkak1XaFhUVWhzY1ZJeWJEQlJWWGd5VTBka1RHSjVTamxNUTBwMVdsaG9NRlZ0Vm1waU0xcHNZMjVzUkdJeU1YUmhXRkowV2xjMU1GTkhSbnBoUTBrMlNXdFdjRkpHVmxwYWExcHlVMWhhVldWR1pHNWhTR2hxV0RFNVZWVkZhelJOVlhoS1dEQnNkRlo2VW5KbFJFSjBWMGRTVGxGWFVqRk1VekV5V1d4RmFXWlJJaXdpY0dGMFkyaEVZWFJoSWpvaVpYbEtkVnBZYURCV1dFSnJXVmhTYkZFeU9YUmlWMnd3WWxkV2RXUkZhR2hqTW1kcFQybEtSbUZWUmpKaFNHaHBZMmt4V0ZSNlFqWldiRGd6VFc1d1IxWnRVbVpqYWxwMlZUTnNlbUV5TVRKaFJ6UTFVMU14YldORmJHcE9NRkl3V0ROYWJrbHBkMmxqUjBZd1dUSm9iR041U1RaWE0zTnBXVmRPTUdGWE9YVkphbTlwWTIxV2QySkhSbXBhVTBselNXMVNkbGt6Vm5SYVZ6VXdTV3B3TjBsdVFqRlpiWGh3V1RCMGJHVllUV2xQYkhRM1NXMXNhMGxxYjJsa01sWnBWRmRXTUdGSE9XdFRNbFkxU1dsM2FXUkliSGRhVTBrMlNXeE9iRmt6UVhsT1ZGcHlUVlphYkdOdGJHMWhWMDVvWkVkc2RtSnJkR3hsVkVsM1RWUm5hVXhEU25Ga01uTnBUMjV6YVdFelVqVkphbTlwVWxWTmFVeERTbXBqYmxscFQybEtlbHBYVG5kTmFsVXlZWHBGYVV4RFNqUkphbTlwV1RBd2RHVlhiRXhSV0djd1ZsVkdNV1ZzWkZwVWVrcEZXbFV4YzFwVWJGUmxSRWw1VjFjNVQxVkZTalphUld4clpVUk9lVk42VGt0aWVVbHpTVzVyYVU5cFNtbFVWVTU0WVVoc1dtUXdSbXRTUm1oTVRsUnJlVTVVUlhkWmJHUkdURmRHUTJSRVNtOU9hMHBFV1Zkd05WZ3dPVXRhTVVaWFVtcGtia2x1TVRsWVUzZHBZekpXZVdSdGJHcGFWVloxV2toQ2RtRlhOVEJqZVVrMlZ6RXhPV1pXTVRraWZRI0hCdXFzLWJQeFFOWEUxX3ppcjNqTEEuc2lnbmF0dXJlLjEifQ.eyJyZXNwb25zZV90eXBlIjoiaWR0b2tlbiIsInJlc3BvbnNlX21vZGUiOiJmb3JtX3Bvc3QiLCJjbGllbnRfaWQiOiJodHRwczovL2xvY2FsaG9zdDozMDAwL3JlcXVlc3QiLCJyZWRpcmVjdF91cmkiOiJodHRwczovL2xvY2FsaG9zdDozMDAwL3ZlcmlmeSIsImlzcyI6ImRpZDppb246RWlBeS1XRWNuaWxWRW51LUJmUWltMHFoT0h0R1FiVTFObGtjeHliRTZRVlFCUT8taW9uLWluaXRpYWwtc3RhdGU9ZXlKMGVYQmxJam9pWTNKbFlYUmxJaXdpYzNWbVptbDRSR0YwWVNJNkltVjVTbmRaV0ZKcVlVVlNhR1JIUmtsWldFNXZTV3B2YVZKWGJFUllNVVl3VVcxNFZFMHpSWFJWUkd4cVlqQndUbVZJV2xCTk1HeFlZbnBrYjJGRlNqQlhWMXB6VDFaS1JGZElRbWhTUkdnMVZGWk9XV1I1U1hOSmJrcHNXVEk1TWxwWVNqVlRNbFkxU1dwd04wbHRkREJsVTBrMlNXdFdSRWxwZDJsWk0wb3lTV3B2YVdNeVZtcGpSRWt4VG0xemVFbHBkMmxsUTBrMlNXeHNjazVWUm10TmJFa3pZa1ZXTUZaRll6UlRWa3BhWTNwS1ZVNHlkekZWUldoTFQxaGtSR1I2VWsxV1YydDVZMnMxTVZaSE1WaGFNbk5wVEVOS05VbHFiMmxXUjFJMlVtdGtWVmxXVG1aWGJWSkRXbGN4Um1OV1JqRlplazU2VWpGa2FWSXhaRmRqTVdoWFRVaHNjVkl5YkRCUlZYZ3lVMGRrVEdKNVNqbE1RMHAxV2xob01GVnRWbXBpTTFwc1kyNXNSR0l5TVhSaFdGSjBXbGMxTUZOSFJucGhRMGsyU1d0V2NGSkdWbHBhYTFweVUxaGFWV1ZHWkc1aFNHaHFXREU1VlZWRmF6Uk5WWGhLV0RCc2RGWjZVbkpsUkVKMFYwZFNUbEZYVWpGTVV6RXlXV3hGYVdaUklpd2ljR0YwWTJoRVlYUmhJam9pWlhsS2RWcFlhREJXV0VKcldWaFNiRkV5T1hSaVYyd3dZbGRXZFdSRmFHaGpNbWRwVDJsS1JtRlZSakpoU0docFkya3hXRlI2UWpaV2JEZ3pUVzV3UjFadFVtWmphbHAyVlROc2VtRXlNVEpoUnpRMVUxTXhiV05GYkdwT01GSXdXRE5hYmtscGQybGpSMFl3V1RKb2JHTjVTVFpYTTNOcFdWZE9NR0ZYT1hWSmFtOXBZMjFXZDJKSFJtcGFVMGx6U1cxU2Rsa3pWblJhVnpVd1NXcHdOMGx1UWpGWmJYaHdXVEIwYkdWWVRXbFBiSFEzU1cxc2EwbHFiMmxrTWxacFZGZFdNR0ZIT1d0VE1sWTFTV2wzYVdSSWJIZGFVMGsyU1d4T2JGa3pRWGxPVkZweVRWWmFiR050YkcxaFYwNW9aRWRzZG1KcmRHeGxWRWwzVFZSbmFVeERTbkZrTW5OcFQyNXphV0V6VWpWSmFtOXBVbFZOYVV4RFNtcGpibGxwVDJsS2VscFhUbmROYWxVeVlYcEZhVXhEU2pSSmFtOXBXVEF3ZEdWWGJFeFJXR2N3VmxWR01XVnNaRnBVZWtwRldsVXhjMXBVYkZSbFJFbDVWMWM1VDFWRlNqWmFSV3hyWlVST2VWTjZUa3RpZVVselNXNXJhVTlwU21sVVZVNTRZVWhzV21Rd1JtdFNSbWhNVGxScmVVNVVSWGRaYkdSR1RGZEdRMlJFU205T2EwcEVXVmR3TlZnd09VdGFNVVpYVW1wa2JrbHVNVGxZVTNkcFl6SldlV1J0YkdwYVZWWjFXa2hDZG1GWE5UQmplVWsyVnpFeE9XWldNVGtpZlEiLCJzY29wZSI6Im9wZW5pZCBkaWRfYXV0aG4iLCJzdGF0ZSI6IkhQMkpPdUQ0LWx5YWZ3Iiwibm9uY2UiOiJ5czhuc2hVWjJPcWxPQSIsImF0dGVzdGF0aW9ucyI6eyJzZWxmSXNzdWVkIjp7ImNsYWltcyI6W3sicmVxdWlyZWQiOnRydWUsImNsYWltIjoibmFtZSJ9XX19LCJpYXQiOiIxNTg4MTA4MjgyIiwiZXhwIjoiMTU4ODE0NDI4MiIsInJlZ2lzdHJhdGlvbiI6eyJjbGllbnRfbmFtZSI6IkRlY2VudHJhbGl6ZWQgSWRlbnRpdHkgVGVhbSIsImNsaWVudF9wdXJwb3NlIjoiR2l2ZSB1cyB0aGlzIGluZm9ybWF0aW9uIHBsZWFzZSAod2l0aCBjaGVycnkgb24gdG9wKSEiLCJ0b3NfdXJpIjoiaHR0cHM6Ly90ZXN0LXJlbHlpbmdwYXJ0eS5henVyZXdlYnNpdGVzLm5ldC90b3MuaHRtbCIsImxvZ29fdXJpIjoiaHR0cHM6Ly90ZXN0LXJlbHlpbmdwYXJ0eS5henVyZXdlYnNpdGVzLm5ldC9pbWFnZXMvZGlkX2xvZ28ucG5nIn19."

    @Test
    fun testRequest() {

        val serializer = Serializer()

        val jws = JwsToken.deserialize(token, Serializer())
        print(jws)
        val content = jws.content()
        val request = serializer.parse(OidcRequestContent.serializer(), content)
        print(request)
    }
}

