package gg.airbrush.server.lib

import net.minestom.server.command.ConsoleSender
import net.minestom.server.permission.PermissionVerifier

class OperatorSender : ConsoleSender() {
    override fun hasPermission(permissionName: String, permissionVerifier: PermissionVerifier?): Boolean {
        return true
    }
}