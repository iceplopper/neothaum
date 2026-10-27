package dev.neothaum.registry;

import dev.neothaum.NeoThaum;
import dev.neothaum.baubles.Baubles;
import dev.neothaum.research.PlayerKnowledge;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Player data: Thaumcraft's .thaum file (knowledge) and the Baubles inventory. */
public final class TVAttachments {
	public static final AttachmentType<PlayerKnowledge> KNOWLEDGE = AttachmentRegistry.<PlayerKnowledge>builder()
		.persistent(PlayerKnowledge.CODEC)
		.copyOnDeath()
		.initializer(PlayerKnowledge::empty)
		.syncWith(PlayerKnowledge.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
		.buildAndRegister(NeoThaum.id("knowledge"));

	public static final AttachmentType<Baubles> BAUBLES = AttachmentRegistry.<Baubles>builder()
		.persistent(Baubles.CODEC)
		.initializer(Baubles::empty)
		.syncWith(Baubles.STREAM_CODEC, AttachmentSyncPredicate.all())
		.buildAndRegister(NeoThaum.id("baubles"));

	public static void init() {
	}
}
