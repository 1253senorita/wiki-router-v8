import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.teminator.mypadnoteone.presentation.barobaro.room.MatchingRoomViewModel

@Composable
fun MatchingRoomScreen(
    roomId: String = "ROOM_TEST_GLOBAL", // 기본값 설정
    viewModel: MatchingRoomViewModel = hiltViewModel(), // Hilt 기본 주입 또는 기본값
    onBackClick: () -> Unit = {} // 빈 람다로 기본값 설정
) {
    // 화면 구현부...
}