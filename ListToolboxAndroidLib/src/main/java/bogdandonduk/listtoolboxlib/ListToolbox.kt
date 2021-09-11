package bogdandonduk.listtoolboxlib

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bogdandonduk.kotlinxcoroutineswrappersandroidlibrary.ScopesAndJobs
import bogdandonduk.randomkeygenerator.RandomKeyGenerator
import kotlinx.coroutines.launch

object ListToolbox {
    private val savedStates = mutableMapOf<String, Parcelable?>()

    @JvmOverloads
    @Synchronized
    fun saveListState(host: Any, key: String = RandomKeyGenerator.generateWithClassPrefix(host), recyclerView: RecyclerView) = key.apply {
        recyclerView.layoutManager?.run {
            savedStates[key] = onSaveInstanceState()
        }
    }

    @Synchronized
    fun restoreSavedListState(key: String, recyclerView: RecyclerView) {
        recyclerView.layoutManager?.onRestoreInstanceState(savedStates[key])

        savedStates.remove(key)
    }

    @Synchronized
    fun removeSavedListState(key: String) {
        savedStates.remove(key)
    }

    @JvmOverloads
    fun <T : RecyclerView.Adapter<out RecyclerView.ViewHolder>> initializeList(
        context: Context,
        recyclerView: RecyclerView,
        adapter: T,
        layoutManager: RecyclerView.LayoutManager? = null,
        changeAnimationsEnabled: Boolean = true,
        canReuseUpdatedViewHolder: Boolean = true
    ) {
        with(recyclerView) {
            if(this.layoutManager == null)
                this.layoutManager = layoutManager ?: LinearLayoutManager(context)

            this.adapter = adapter

            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder) = canReuseUpdatedViewHolder
            }

            (itemAnimator as DefaultItemAnimator).run {
                supportsChangeAnimations = changeAnimationsEnabled
            }
        }
    }

    fun <T: RecyclerView.Adapter<out RecyclerView.ViewHolder>> updateLists(async: Boolean = false, vararg adapters: T) {
        adapters.forEach {
            if(!async)
                for(i in 0 until it.itemCount) {
                    it.notifyItemChanged(i)
                }
            else
                ScopesAndJobs.getMainScope().launch {
                    for(i in 0 until it.itemCount) {
                        it.notifyItemChanged(i)
                    }
                }
        }
    }

    inline fun <T : View> operateOnPositionedTargetView(recyclerView: RecyclerView, position: Int, @IdRes targetViewId: Int, action: (targetView: T?) -> Unit) {
        action.invoke(recyclerView.layoutManager?.findViewByPosition(position)?.findViewById(targetViewId) as? T)
    }

    inline fun operateOnPositionedRootView(recyclerView: RecyclerView, position: Int, action: (rootView: View?) -> Unit) {
        action.invoke(recyclerView.layoutManager?.findViewByPosition(position))
    }

    fun <T : View> getPositionedTargetView(recyclerView: RecyclerView, position: Int, @IdRes targetViewId: Int) = recyclerView.layoutManager?.findViewByPosition(position)?.findViewById(targetViewId) as? T

    fun getPositionedRootView(recyclerView: RecyclerView, position: Int) = recyclerView.layoutManager?.findViewByPosition(position)
}