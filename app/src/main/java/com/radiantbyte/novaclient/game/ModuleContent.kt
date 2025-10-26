package com.radiantbyte.novaclient.game

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.radiantbyte.novaclient.R
import com.radiantbyte.novaclient.overlay.OverlayManager
import com.radiantbyte.novaclient.util.translatedSelf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val moduleCache = HashMap<ModuleCategory, List<Module>>()

private fun fetchCachedModules(moduleCategory: ModuleCategory): List<Module> {
    val cachedModules = moduleCache[moduleCategory] ?: ModuleManager.modules
        .filter {
            !it.private && it.category === moduleCategory
        }
    moduleCache[moduleCategory] = cachedModules
    return cachedModules
}

@Composable
fun ModuleContent(moduleCategory: ModuleCategory) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val columnCount = sharedPreferences.getInt("module_columns", 3)

    var modules by remember { mutableStateOf<List<Module>?>(null) }

    LaunchedEffect(moduleCategory) {
        withContext(Dispatchers.IO) {
            modules = fetchCachedModules(moduleCategory)
        }
    }

    Crossfade(targetState = modules) {
        if (it != null) {
            Box(Modifier.fillMaxSize()) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Use configured column count
                    for (columnIndex in 0 until columnCount) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val columnModules = it.filterIndexed { index, _ ->
                                index % columnCount == columnIndex
                            }

                            items(columnModules.size) { index ->
                                ModuleCard(columnModules[index])
                            }
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(module: Module) {
    val values = module.values
    val background by animateColorAsState(
        targetValue = if (module.isExpanded) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.background
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    module.isExpanded = !module.isExpanded
                }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = background
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Text(
                    module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier,
                    color = if (module.isExpanded) Color.White else contentColorFor(
                        MaterialTheme.colorScheme.background
                    )
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = {
                        module.isEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = if (module.isExpanded) Color.White else Color.Transparent,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = if (module.isExpanded) Color.White else MaterialTheme.colorScheme.outline,
                        uncheckedThumbColor = if (module.isExpanded) Color.White else MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                )
            }
            if (module.isExpanded) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(it)
                        is FloatValue -> FloatValueContent(it)
                        is IntValue -> IntValueContent(it)
                        is ListValue -> ChoiceValueContent(it)
                        is EnumValue<*> -> EnumValueContent(it)
                        is StringValue -> StringValueContent(it)
                    }
                }
                ShortcutContent(module)
            }
        }
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    Column(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            value.listItems.forEach {
                ElevatedFilterChip(
                    selected = value.value == it,
                    onClick = {
                        if (value.value != it) {
                            value.value = it
                        }
                    },
                    label = {
                        Text(it.name.translatedSelf)
                    },
                    modifier = Modifier.height(30.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatValueContent(value: FloatValue) {
    Column(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
    ) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        val colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            activeTickColor = Color.White,
            inactiveTickColor = Color.Gray,
            inactiveTrackColor = Color.Gray
        )
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = animateFloatAsState(
                targetValue = value.value,
                label = "",
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow
                )
            ).value,
            valueRange = value.range,
            colors = colors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = DpSize(4.dp, 22.dp),
                    enabled = true
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = colors,
                    enabled = true,
                    sliderState = sliderState,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 4.dp
                )
            },
            onValueChange = {
                val newValue = ((it * 100.0).roundToInt() / 100.0).toFloat()
                if (value.value != newValue) {
                    value.value = newValue
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntValueContent(value: IntValue) {
    Column(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
    ) {
        Row {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        val colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            activeTickColor = Color.White,
            inactiveTickColor = Color.Gray,
            inactiveTrackColor = Color.Gray
        )
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = animateFloatAsState(
                targetValue = value.value.toFloat(),
                label = "",
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow
                )
            ).value,
            valueRange = value.range.toFloatRange(),
            colors = colors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = DpSize(4.dp, 22.dp),
                    enabled = true
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = colors,
                    enabled = true,
                    sliderState = sliderState,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 4.dp
                )
            },
            onValueChange = {
                val newValue = it.roundToInt()
                if (value.value != newValue) {
                    value.value = newValue
                }
            }
        )
    }
}

@Composable
private fun BoolValueContent(value: BoolValue) {
    Row(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
            .toggleable(
                value = value.value,
                interactionSource = null,
                indication = null,
                onValueChange = {
                    value.value = it
                }
            )
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color.White,
                checkedColor = Color.White,
                checkmarkColor = Color.Black
            )
        )
    }
}

@Composable
private fun ShortcutContent(module: Module) {
    Row(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .toggleable(
                value = module.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = {
                    module.isShortcutDisplayed = it
                    if (it) {
                        OverlayManager.showOverlayWindow(module.overlayShortcutButton)
                    } else {
                        OverlayManager.dismissOverlayWindow(module.overlayShortcutButton)
                    }
                }
            )
    ) {
        Text(
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier
                .padding(0.dp),
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color.White,
                checkedColor = Color.White,
                checkmarkColor = Color.Black
            )
        )
    }
}

@Composable
private fun <T : Enum<T>> EnumValueContent(value: EnumValue<T>) {
    Column(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            value.enumClass.enumConstants?.forEach { enumValue ->
                ElevatedFilterChip(
                    selected = value.value == enumValue,
                    onClick = {
                        if (value.value != enumValue) {
                            value.value = enumValue
                        }
                    },
                    label = {
                        Text(enumValue.name.translatedSelf)
                    },
                    modifier = Modifier.height(30.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun StringValueContent(value: StringValue) {
    Column(
        Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()