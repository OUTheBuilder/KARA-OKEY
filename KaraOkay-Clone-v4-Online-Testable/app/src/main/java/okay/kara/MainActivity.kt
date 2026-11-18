package okay.kara

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder

private val Bg = Color(0xFF061A15)
private val Panel = Color(0xFF0D2A23)
private val Panel2 = Color(0xFF10382E)
private val Accent = Color(0xFF67E3B9)
private val Muted = Color(0xFFB8D7CD)
private val Header = Color(0xFF0A5A45)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("karaokay", MODE_PRIVATE)
        setContent { KaraOkayCloneApp(prefs) }
    }
}

private enum class Screen(val title: String, val icon: String) {
    Library("Songs", "♫"),
    Online("Online", "▶"),
    Music("Music", "♪"),
    Remote("Remote", "⌁"),
    Recordings("Recordings", "●"),
    Settings("Settings", "⚙")
}

@Composable
private fun KaraOkayCloneApp(prefs: android.content.SharedPreferences) {
    var screen by remember { mutableStateOf(Screen.Library) }
    var menuOpen by remember { mutableStateOf(false) }
    var onlineMode by remember { mutableStateOf(prefs.getBoolean("online_mode", false)) }

    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    onlineMode = onlineMode,
                    onMenu = { menuOpen = !menuOpen }
                )
                if (menuOpen) {
                    SideMenu(
                        current = screen,
                        onSelect = { screen = it; menuOpen = false }
                    )
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (screen) {
                        Screen.Library -> LibraryScreen(onlineMode = onlineMode, onOnline = { screen = Screen.Online })
                        Screen.Online -> OnlineYouTubeScreen(onlineMode = onlineMode, onOnlineModeChanged = { onlineMode = it; prefs.edit().putBoolean("online_mode", it).apply() })
                        Screen.Music -> MusicScreen()
                        Screen.Remote -> RemoteScreen()
                        Screen.Recordings -> RecordingsScreen()
                        Screen.Settings -> SettingsScreen(onlineMode = onlineMode, onOnlineModeChanged = { onlineMode = it; prefs.edit().putBoolean("online_mode", it).apply() })
                    }
                }
                BottomNav(current = screen, onlineMode = onlineMode, onSelect = { screen = it; menuOpen = false })
            }
        }
    }
}

@Composable
private fun TopBar(onlineMode: Boolean, onMenu: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Header).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.karaokay_logo),
            contentDescription = "Kara-okay",
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("KARA-OKAY", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("VERSION 1.9.0", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            if (onlineMode) "ONLINE" else "OFFLINE",
            color = if (onlineMode) Accent else Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(12.dp))
        Text("☰", color = Color.White, fontSize = 28.sp, modifier = Modifier.clickable { onMenu() })
    }
}

@Composable
private fun SideMenu(current: Screen, onSelect: (Screen) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color(0xFF08231D)).padding(8.dp)) {
        Screen.entries.forEach { s ->
            val selected = current == s
            Row(
                Modifier.fillMaxWidth().background(if (selected) Panel2 else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(s) }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.icon, color = Accent, fontSize = 20.sp, modifier = Modifier.width(34.dp))
                Text(s.title, color = Color.White, fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun BottomNav(current: Screen, onlineMode: Boolean, onSelect: (Screen) -> Unit) {
    val screens = listOf(Screen.Library, Screen.Online, Screen.Music, Screen.Remote, Screen.Recordings, Screen.Settings)
    NavigationBar(containerColor = Color(0xFF041611)) {
        screens.forEach { s ->
            NavigationBarItem(
                selected = current == s,
                onClick = { onSelect(s) },
                enabled = s != Screen.Online || onlineMode,
                icon = { Text(s.icon, fontSize = 20.sp) },
                label = { Text(s.title, fontSize = 9.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                    disabledIconColor = Muted.copy(alpha = .35f),
                    disabledTextColor = Muted.copy(alpha = .35f)
                )
            )
        }
    }
}

@Composable
private fun LibraryScreen(onlineMode: Boolean, onOnline: () -> Unit) {
    var tab by remember { mutableStateOf("Your songs") }
    val tabs = listOf("Your songs", "All songs", "Favorites", "Up next")
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            tabs.forEach { label ->
                FilterChip(selected = tab == label, onClick = { tab = label }, label = { Text(label, fontSize = 11.sp) })
            }
        }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            item { IntroCard(onlineMode, onOnline) }
            items(listOf("Song list", "Search a title, artist or number", "Playlists", "Song menu")) { title ->
                ActionCard(title)
            }
        }
    }
}

@Composable
private fun IntroCard(onlineMode: Boolean, onOnline: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Panel2), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Your songs", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Add your songs: send them from your phone, or from a USB drive's Kara-okay folder.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)) { Text("Add your songs") }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onOnline, enabled = !onlineMode, modifier = Modifier.fillMaxWidth()) {
                Text(if (onlineMode) "Online mode is enabled" else "Search YouTube karaoke online")
            }
        }
    }
}

@Composable
private fun ActionCard(title: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text("›", color = Accent, fontSize = 28.sp)
        }
    }
}

@Composable
private fun OnlineYouTubeScreen(onlineMode: Boolean, onOnlineModeChanged: (Boolean) -> Unit) {
    var query by remember { mutableStateOf("") }
    var searchUrl by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Online Karaoke", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Search YouTube for karaoke videos", color = Accent, fontSize = 12.sp)
            }
            Switch(checked = onlineMode, onCheckedChange = onOnlineModeChanged)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Karaoke search") },
                placeholder = { Text("e.g. My Way karaoke") }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = onlineMode && query.isNotBlank(),
                onClick = {
                    val encoded = URLEncoder.encode("${query.trim()} karaoke", "UTF-8")
                    searchUrl = "https://m.youtube.com/results?search_query=$encoded"
                }
            ) { Text("Search") }
        }
        Spacer(Modifier.height(8.dp))
        if (!onlineMode) {
            Card(colors = CardDefaults.cardColors(containerColor = Panel2), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Offline mode: local songs remain available. Turn Online mode on to search YouTube.",
                    color = Muted,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else if (searchUrl == null) {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Enter a song, artist, or karaoke title above. Search results are loaded from YouTube while online.",
                    color = Muted,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            YouTubeWebView(searchUrl!!, Modifier.fillMaxSize())
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeWebView(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = true
                settings.userAgentString = settings.userAgentString + " KaraOkayClone/1.0"
                CookieManager.getInstance().setAcceptCookie(true)
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) webView.loadUrl(url)
        }
    )
}

@Composable
private fun MusicScreen() {
    SimplePage("Music", "Music", listOf("Music library", "Playing:", "Play next", "Sound and playback"))
}

@Composable
private fun RemoteScreen() {
    SimplePage("Remote", "Remote", listOf("Remote control", "Find by Bluetooth", "Best on Wi-Fi.", "Both must be on the same Wi-Fi, or one on the other's hotspot."))
}

@Composable
private fun RecordingsScreen() {
    SimplePage("Recordings", "Recordings", listOf("Your recordings", "Record your voice with the song", "Stage video", "Recordings stay on this phone."))
}

@Composable
private fun SettingsScreen(onlineMode: Boolean, onOnlineModeChanged: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Connection mode", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(if (onlineMode) "Online — YouTube search available" else "Offline — local library only", color = Muted, fontSize = 12.sp)
            }
            Switch(checked = onlineMode, onCheckedChange = onOnlineModeChanged)
        }
        SimplePage("Settings", "Settings", listOf("Songs, sounds and backdrops", "Lyric settings", "Music settings", "Sound settings", "Stage settings", "Camera", "Score"))
    }
}

@Composable
private fun SimplePage(title: String, subtitle: String, rows: List<String>) {
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Accent, fontSize = 12.sp)
        }
        items(rows) { ActionCard(it) }
    }
}
