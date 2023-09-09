package de.mannodermaus.dk23

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mannodermaus.dk23.models.User
import de.mannodermaus.dk23.ui.theme.DK23Theme
import me.xdrop.jrand.JRand
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DK23Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(Modifier.fillMaxSize())
                }
            }
        }
    }
}

class MainViewModel : ViewModel() {
    val users = mutableStateListOf<User>()
}

@Composable
private fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    Box(modifier) {
        Column(Modifier.fillMaxSize()) {
            UserAnnotations()
            Divider()
            UserList(viewModel.users)
        }

        AddUserButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onUserAdded = viewModel.users::add,
        )
    }
}

@Composable
private fun UserAnnotations() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val allUserClassAnnotations = rememberAnnotationsOfUserClass()

    val dropdownArrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Dropdown Arrow Rotation",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = buildAnnotatedString {
                        append("Annotations of ")
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                            append("User")
                        }
                        append(" (${allUserClassAnnotations.size})")
                    },
                    fontWeight = FontWeight.Bold,
                )

                Icon(
                    modifier = Modifier.rotate(dropdownArrowRotation),
                    painter = rememberVectorPainter(Icons.Filled.ArrowDropDown),
                    contentDescription = null
                )
            }

            AnimatedContent(
                modifier = Modifier.fillMaxWidth(),
                targetState = expanded,
                transitionSpec = { expandVertically() togetherWith shrinkVertically() },
                label = "Content",
            ) { e ->
                if (e) {
                    Column(Modifier.fillMaxWidth()) {
                        allUserClassAnnotations.forEach { annotation ->
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = annotation,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<User>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        item {
            ListRow(
                modifier = Modifier.padding(bottom = 8.dp),
                isHeader = true,
                topLine = "User",
                bottomLine = "toString()",
            )
        }

        items(users) { user ->
            ListRow(
                topLine = user.name,
                bottomLine = user.toString(),
            )
        }
    }
}

@Composable
private fun ListRow(
    topLine: String,
    bottomLine: String,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val color = if (isHeader) MaterialTheme.colorScheme.primary else Color.Unspecified
    val style = if (isHeader) MaterialTheme.typography.bodySmall else LocalTextStyle.current
    val bottomWeight = if (isHeader) FontWeight.Bold else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = topLine,
            fontWeight = FontWeight.Bold,
            color = color,
            style = style,
        )

        Text(
            modifier = Modifier.weight(3f),
            text = bottomLine,
            fontWeight = bottomWeight,
            color = color,
            style = style,
        )
    }
}

@Composable
private fun AddUserButton(
    onUserAdded: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val createRandomUser: () -> User = remember {
        {
            User(
                JRand.firstname().gen(),
                JRand.age().adult().gen(),
                Random.nextPassword(8),
                Random.nextInt(from = 1000, until = 9999),
            )
        }
    }

    FloatingActionButton(
        modifier = modifier,
        onClick = { onUserAdded(createRandomUser()) },
    ) {
        Icon(
            painter = rememberVectorPainter(Icons.Filled.Add),
            contentDescription = "Add"
        )
    }
}

@Composable
private fun rememberAnnotationsOfUserClass() = remember {
    User::class.java.declaredAnnotations.mapNotNull { a -> a.annotationClass.qualifiedName }
}

private fun Random.nextPassword(len: Int) =
    IntArray(len) {
        nextInt(from = '0'.code, until = 'z'.code)
    }.joinToString(separator = "") { it.toChar().toString() }

/* Previews */

@Preview
@Composable
private fun UserClassMethods_Preview() {
    MaterialTheme {
        UserAnnotations()
    }
}
