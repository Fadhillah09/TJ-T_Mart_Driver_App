package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R

class ChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatVH>() {

    // ViewHolder types
    companion object {
        private const val TYPE_DRIVER   = 1  // Driver (kanan, merah)
        private const val TYPE_CUSTOMER = 2  // Customer (kiri, abu)
    }

    override fun getItemViewType(position: Int): Int =
        if (messages[position].pengirim == "driver") TYPE_DRIVER else TYPE_CUSTOMER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val layoutId = if (viewType == TYPE_DRIVER)
            R.layout.item_chat_driver else R.layout.item_chat_customer
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ChatVH(view)
    }

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    class ChatVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvIsi: TextView?      = itemView.findViewById(R.id.tvIsiPesan)
        private val tvWaktu: TextView?    = itemView.findViewById(R.id.tvWaktuPesan)
        private val ivMedia: ImageView?   = itemView.findViewById(R.id.ivMediaPesan)
        private val badgeVideo: TextView? = itemView.findViewById(R.id.tvBadgeVideo)

        fun bind(msg: ChatMessage) {
            when (msg.tipe) {
                TipePesan.TEKS -> {
                    tvIsi?.text       = msg.isi
                    tvIsi?.visibility = View.VISIBLE
                    ivMedia?.visibility  = View.GONE
                    badgeVideo?.visibility = View.GONE
                }

                TipePesan.GAMBAR -> {
                    tvIsi?.visibility   = View.GONE
                    ivMedia?.visibility = View.VISIBLE
                    badgeVideo?.visibility = View.GONE
                    try {
                        ivMedia?.setImageURI(Uri.parse(msg.isi))
                    } catch (e: Exception) {
                        ivMedia?.setImageResource(R.drawable.ic_photo_camera)
                    }
                }

                TipePesan.VIDEO -> {
                    tvIsi?.visibility      = View.GONE
                    ivMedia?.visibility    = View.VISIBLE
                    badgeVideo?.visibility = View.VISIBLE
                    ivMedia?.setImageResource(R.drawable.ic_videocam)
                }
            }
            tvWaktu?.text = msg.waktu
        }
    }
}