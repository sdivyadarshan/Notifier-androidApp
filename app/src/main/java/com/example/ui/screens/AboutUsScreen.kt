package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.AllowedGreen
import com.example.ui.theme.ElegantDarkAvatarBg
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkOutlineVariant
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceHighlight
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary

private const val CREATOR_NAME = "S DIVYA DARSHAN"
private const val CREATOR_EMAIL = "sddarshan2007@gmail.com"
private const val CREATOR_BIO = "A Guy with interests in technology, creative problem-solving, and digital content creation. Skilled in presentation design, basic video editing, and exploring innovative ideas through creativity and technical learning."

private const val CREATOR_IMAGE_DIRECT_URL = "https://i.postimg.cc/Sx4bWdCT/b18cecfb-6587-475f-a008-816107cb8063.jpg"
private const val LOGO_IMAGE_URL = "https://i.postimg.cc/bwP8N3zM/IMG-20260907-010212-768.png"

private const val INSTAGRAM_URL = "https://www.instagram.com/_.daxrshnxz?stkn=MW5uZWlndWxnczY4eA=="
private const val LINKEDIN_URL = "https://www.linkedin.com/in/s-divyadarshan-796076376?utm_source=share_via&utm_content=profile&utm_medium=member_android"

@Composable
fun AboutUsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("about_us_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Logo & Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_logo_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF833AB4),
                                        Color(0xFFFD1D1D),
                                        Color(0xFFFCB045)
                                    )
                                )
                            )
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(R.drawable.app_logo)
                                .crossfade(true)
                                .placeholder(R.drawable.app_logo)
                                .error(R.drawable.app_logo)
                                .build(),
                            contentDescription = "Notifier App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(ElegantDarkBackground)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Notifier",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Real-Time Instagram Notification Filter",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AllowedGreen)
                        )
                        Text(
                            text = "100% On-Device • Zero Cloud Tracking • Pure Privacy",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = AllowedGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Developer Section (Matches User Reference Image Design)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_developer_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // DEVELOPER tag
                    Text(
                        text = "DEVELOPER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = AllowedGreen,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Avatar + Social Links
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Avatar with glowing gradient ring
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            Color(0xFFFF007F),
                                            Color(0xFF7928CA),
                                            Color(0xFF0070F3),
                                            Color(0xFFFF007F)
                                        )
                                    )
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(R.drawable.creator_photo)
                                    .crossfade(true)
                                    .placeholder(R.drawable.creator_photo)
                                    .error(R.drawable.creator_photo)
                                    .fallback(R.drawable.creator_photo)
                                    .build(),
                                contentDescription = "Creator S Divya Darshan",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(ElegantDarkAvatarBg)
                            )
                        }

                        // Social media icons: Instagram & LinkedIn
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Instagram Button
                            SocialIconButton(
                                containerColor = Color(0xFF331626),
                                borderColor = Color(0xFFC13584).copy(alpha = 0.6f),
                                iconColor = Color(0xFFE1306C),
                                label = "IG",
                                isInstagram = true,
                                onClick = {
                                    openUrl(context, INSTAGRAM_URL)
                                }
                            )

                            // LinkedIn Button
                            SocialIconButton(
                                containerColor = Color(0xFF10283E),
                                borderColor = Color(0xFF0A66C2).copy(alpha = 0.6f),
                                iconColor = Color(0xFF3897F0),
                                label = "in",
                                isInstagram = false,
                                onClick = {
                                    openUrl(context, LINKEDIN_URL)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Name
                    Text(
                        text = CREATOR_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = ElegantDarkTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email (Clickable to send email)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                sendEmail(context, CREATOR_EMAIL)
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AllowedGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = CREATOR_EMAIL,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AllowedGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bio
                    Text(
                        text = CREATOR_BIO,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElegantDarkTextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // APP INFO Section (Exact match with reference image)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_app_info_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "APP INFO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = AllowedGreen,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AppInfoRow(label = "Version", value = "1.0.0", highlight = false)
                    AppInfoDivider()
                    AppInfoRow(label = "Platform", value = "Android (Jetpack Compose)", highlight = true)
                    AppInfoDivider()
                    AppInfoRow(label = "Storage", value = "Local only — no cloud sync", highlight = true)
                    AppInfoDivider()
                    AppInfoRow(label = "Data collected", value = "None (100% Private)", highlight = true)
                }
            }
        }

        // Footer: Made with Heart
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Made with ❤️ in India — Notifier v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Designed for digital wellness & uninterrupted focus",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElegantDarkTextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SocialIconButton(
    containerColor: Color,
    borderColor: Color,
    iconColor: Color,
    label: String,
    isInstagram: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(if (isInstagram) "social_button_instagram" else "social_button_linkedin"),
        contentAlignment = Alignment.Center
    ) {
        if (isInstagram) {
            // Instagram Camera Glyph
            InstagramGlyph(color = iconColor)
        } else {
            // LinkedIn "in" Typography
            Text(
                text = "in",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = iconColor
            )
        }
    }
}

@Composable
private fun InstagramGlyph(color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .border(2.dp, color, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle
        Box(
            modifier = Modifier
                .size(10.dp)
                .border(2.dp, color, CircleShape)
        )
        // Camera dot top right
        Box(
            modifier = Modifier
                .size(3.dp)
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 2.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
private fun AppInfoRow(
    label: String,
    value: String,
    highlight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ElegantDarkTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) ElegantDarkTextPrimary else ElegantDarkTextSecondary
        )
    }
}

@Composable
private fun AppInfoDivider() {
    HorizontalDivider(
        color = ElegantDarkOutlineVariant.copy(alpha = 0.6f),
        thickness = 0.8.dp
    )
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}

private fun sendEmail(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Email: $email", Toast.LENGTH_SHORT).show()
    }
}
