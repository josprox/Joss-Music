package it.vfsfitvnm.vimusic.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.shimmer
import it.vfsfitvnm.vimusic.utils.secondary
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import it.vfsfitvnm.innertube.Innertube

@Composable
fun AlbumItem(
    album: Album,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnailUrl,
        title = album.title,
        authors = album.authorsText,
        year = album.year,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier
    )
}

@Composable
fun AlbumItem(
    album: Innertube.AlbumItem,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    AlbumItem(
        thumbnailUrl = album.thumbnail?.url,
        title = album.info?.name,
        authors = album.authors?.joinToString("") { it.name ?: "" },
        year = album.year,
        thumbnailSizePx = thumbnailSizePx,
        thumbnailSizeDp = thumbnailSizeDp,
        alternative = alternative,
        modifier = modifier
    )
}

@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    thumbnailSizePx: Int,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (_, typography, _) = LocalAppearance.current

    // Aplicar bordes redondeados al contenedor principal (ItemContainer)
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)) // Borde redondeado general para el contenedor
            .padding(8.dp) // Espaciado dentro del contenedor
    ) {
        // Imagen del álbum con bordes redondeados
        AsyncImage(
            model = thumbnailUrl?.thumbnail(thumbnailSizePx),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp)) // Borde redondeado para la imagen
                .size(thumbnailSizeDp)
        )

        // Contenedor de la información del álbum
        ItemInfoContainer {
            BasicText(
                text = title ?: "",
                style = typography.xs.semiBold,
                maxLines = if (alternative) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!alternative) {
                authors?.let {
                    BasicText(
                        text = authors,
                        style = typography.xs.semiBold.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            BasicText(
                text = year ?: "",
                style = typography.xxs.semiBold.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AlbumItemPlaceholder(
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) {
    val (colorPalette, _, _) = LocalAppearance.current

    // Aplicar bordes redondeados al placeholder también
    ItemContainer(
        alternative = alternative,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)) // Borde redondeado para el contenedor placeholder
            .background(Color.Gray)
            .padding(8.dp)
    ) {
        Spacer(
            modifier = Modifier
                .background(color = colorPalette.shimmer, shape = RoundedCornerShape(12.dp))
                .size(thumbnailSizeDp)
        )

        ItemInfoContainer {
            TextPlaceholder()

            if (!alternative) {
                TextPlaceholder()
            }

            TextPlaceholder(
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}
