package com.example

import com.example.data.model.InviteCodeConfig
import com.example.ui.components.extractInviteCodeFromQr
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun inviteCodeConfig_generatesValid8CharCodeWithoutConfusingChars() {
    repeat(20) {
      val code = InviteCodeConfig.generate()
      assertEquals(8, code.length)
      assertTrue(InviteCodeConfig.isValidFormat(code))
      assertFalse(code.contains('O'))
      assertFalse(code.contains('0'))
      assertFalse(code.contains('I'))
      assertFalse(code.contains('1'))
      assertFalse(code.contains('L'))
    }
    assertEquals("K7M9P2XQ", InviteCodeConfig.normalize("  k7m9 p2xq \n"))
    assertEquals("K7M9P2XQ", InviteCodeConfig.sanitizeInput("k7m9-p2xq99"))
  }

  @Test
  fun extractInviteCodeFromQr_handlesDirectAndFormatted8CharCodes() {
    assertEquals("K7M9P2XQ", extractInviteCodeFromQr("K7M9P2XQ"))
    assertEquals("ABCDEF23", extractInviteCodeFromQr("  abcdef23 \n"))
    assertEquals("ZX78QP4W", extractInviteCodeFromQr("Join StoreOS code: zx78qp4w"))
  }
}
