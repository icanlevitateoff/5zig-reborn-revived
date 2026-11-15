/*
 * Copyright (c) 2026 IcanLevitate
 *
 * This file is part of The 5zig Mod
 * The 5zig Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The 5zig Mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The 5zig Mod.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.icanlevitateoff.fivezigrebornrevived.fabric.mixin;

import io.icanlevitateoff.fivezigrebornrevived.mod.crashreport.CrashReportUploader;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uploads the crash report of a client crash once it has been written (see CrashReportUploader). */
@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

	//? if >=1.20.5 {
	@Inject(method = "saveToFile(Ljava/nio/file/Path;Lnet/minecraft/ReportType;Ljava/util/List;)Z", at = @At("RETURN"))
	private void the5zig$onSaved(java.nio.file.Path path, net.minecraft.ReportType type, java.util.List<String> extraLines, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			CrashReportUploader.onCrashReportSaved(path.toFile());
		}
	}
	//?} else {
	/*@Inject(method = "saveToFile(Ljava/io/File;)Z", at = @At("RETURN"))
	private void the5zig$onSaved(java.io.File file, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			CrashReportUploader.onCrashReportSaved(file);
		}
	}
	*///?}
}
