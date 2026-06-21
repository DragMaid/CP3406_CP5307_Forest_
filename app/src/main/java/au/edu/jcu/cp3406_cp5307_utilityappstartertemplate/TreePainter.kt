package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

// ponytail: Dynamic Canvas drawing with mathematical sway and particle calculations. Zero image files.

@Composable
fun TreeCanvas(
    species: TreeSpecies,
    stage: TreeStage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TreeSway")
    

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val groundY = height - 24.dp.toPx()
        val centerX = width / 2f

        // Draw Soil / Ground Line
        drawRect(
            color = Color(0xFF6D4C41),
            topLeft = Offset(0f, groundY),
            size = Size(width, 24.dp.toPx())
        )
        drawRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(0f, groundY + 4.dp.toPx()),
            size = Size(width, 20.dp.toPx())
        )

        // Sway pivot point is the base of the trunk at the ground level
        val pivot = Offset(centerX, groundY)


                TreeStage.MATURE -> {
                    // Thick trunk
                    val trunkWidth = 16.dp.toPx()
                    val trunkHeight = 85.dp.toPx()
                    val trunkColor = if (species == TreeSpecies.BIRCH) Color(0xFFF5F5F5) else Color(0xFF4E342E)

                    drawRect(
                        color = trunkColor,
                        topLeft = Offset(centerX - trunkWidth / 2, groundY - trunkHeight),
                        size = Size(trunkWidth, trunkHeight)
                    )


        }
    }
}
