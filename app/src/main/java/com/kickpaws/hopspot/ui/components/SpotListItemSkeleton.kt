package com.kickpaws.hopspot.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpotListItemSkeleton() {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail skeleton
            ShimmerBox(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info skeleton
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating
                ShimmerBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Icons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(40.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(50.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }

            // Chevron placeholder
            ShimmerBox(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
fun SpotListSkeleton(
    itemCount: Int = 6
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SpotListItemSkeleton()
        }
    }
}
