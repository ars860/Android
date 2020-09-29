package com.example.contactsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact.view.*


data class Contact(val name: String, val phoneNumber: String)

class ContactViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
    fun bind(contact: Contact) {
        with(root) {
            name.text = contact.name
            phoneNumber.text = contact.phoneNumber
        }
    }
}

class ContactAdapter(
    private val contacts: List<Contact>,
    private val onClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val holder = ContactViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.contact, parent, false)
        )
        holder.root.setOnClickListener {
            onClick(contacts[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(contacts[position])
}

class MainActivity : AppCompatActivity() {
    private enum class Permission(val permissionName: String) {
        CONTACT_PERMISSION(Manifest.permission.READ_CONTACTS),
        CALL_PERMISSION(Manifest.permission.CALL_PHONE)
    }

    private var contacts: MutableList<Contact> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateContacts()

        val viewManager = LinearLayoutManager(this)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = ContactAdapter(contacts) {
                dialNumber(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        for (keyValue in permissionToCallbacks) {
            with(keyValue) {
                if (requestCode == key.ordinal) {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        value.first()
                    } else {
                        value.second()
                    }
                }
            }
        }
    }

    private val permissionToCallbacks: MutableMap<Permission, Pair<() -> Unit, () -> Unit>> =
        mutableMapOf()

    private fun checkPermissionAndDo(
        permission: Permission,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        permissionToCallbacks[permission] = Pair(onSuccess, onFailure)
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission.permissionName
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(permission.permissionName),
                permission.ordinal
            )

        } else {
            onSuccess()
        }
    }

    private fun updateContacts() {
        checkPermissionAndDo(Permission.CONTACT_PERMISSION, {
            contacts.clear()
            contacts.addAll(fetchAllContacts())
            recyclerView.adapter?.notifyDataSetChanged()

            Toast.makeText(
                this@MainActivity,
                resources.getQuantityString(
                    R.plurals.on_get_contacts_success,
                    contacts.size,
                    contacts.size
                ),
                Toast.LENGTH_SHORT
            ).show()
        }, {
            Toast.makeText(
                this@MainActivity,
                resources.getText(R.string.on_get_contacts_failure),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun dialNumber(contact: Contact) {
        checkPermissionAndDo(Permission.CALL_PERMISSION, {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.phoneNumber))
            startActivity(intent)
        }, {
            Toast.makeText(
                this@MainActivity,
                resources.getText(R.string.on_call_failure),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun Context.fetchAllContacts(): List<Contact> {
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
            .use { cursor ->
                if (cursor == null) return emptyList()
                val builder = ArrayList<Contact>()
                while (cursor.moveToNext()) {
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            ?: "N/A"
                    val phoneNumber =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            ?: "N/A"

                    builder.add(Contact(name, phoneNumber))
                }
                return builder
            }
    }
}