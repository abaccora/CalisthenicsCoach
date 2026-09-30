package com.rushd.calisthenicscoach.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rushd.calisthenicscoach.domain.ExerciseVideoCatalog
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseVideoPlaybackInstrumentedTest {

    @Test
    fun everyBundledExerciseVideoCanDecodeAFrame() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        ExerciseVideoCatalog.all.forEach { video ->
            val resId = video.resId
            if (resId != null) {
                val uri = Uri.parse("android.resource://${context.packageName}/$resId")
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    val duration = retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L
                    assertTrue("Invalid duration for ${video.nameEn}", duration > 0L)

                    val frame = retriever.getFrameAtTime(
                        0,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    assertNotNull("Unable to decode video frame for ${video.nameEn}", frame)
                    frame?.recycle()
                } finally {
                    retriever.release()
                }
            }
        }
    }
}
