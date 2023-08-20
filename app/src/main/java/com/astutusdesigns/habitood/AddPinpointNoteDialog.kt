import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.astutusdesigns.habitood.R
import com.google.android.material.textfield.TextInputEditText

class AddPinpointNoteDialog: DialogInterface.OnClickListener {

    private var noteCallback: ((String) -> Unit)? = null
    private var input: TextInputEditText? = null

    fun createDialog(context: Context, noteCallback: (String) -> Unit): AlertDialog {
        this.noteCallback = noteCallback

        return AlertDialog.Builder(context).create().apply {
            setTitle(context.getString(R.string.add_note))

            val view = LayoutInflater.from(context).inflate(R.layout.layout_pinpoint_note, null)
            input = view.findViewById(R.id.anonymousNoteInput)

            setButton(
                AlertDialog.BUTTON_POSITIVE,
                context.getString(android.R.string.ok),
                this@AddPinpointNoteDialog
            )
            setButton(
                AlertDialog.BUTTON_NEGATIVE,
                context.getString(R.string.no_thanks),
                this@AddPinpointNoteDialog
            )

            setView(view)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(which) {
            AlertDialog.BUTTON_POSITIVE -> noteCallback?.invoke(input?.text?.toString() ?: "")
            AlertDialog.BUTTON_NEGATIVE -> noteCallback?.invoke("")
        }
    }
}