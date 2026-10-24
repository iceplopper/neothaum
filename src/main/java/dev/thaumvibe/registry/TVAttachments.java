package dev.thaumvibe.registry;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.baubles.Baubles;
import dev.thaumvibe.research.PlayerKnowledge;
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
		.buildAndRegister(Thaumvibe.id("knowledge"));

	public static final AttachmentType<Baubles> BAUBLES = AttachmentRegistry.<Baubles>builder()
		.persistent(Baubles.CODEC)
		.initializer(Baubles::empty)
		.syncWith(Baubles.STREAM_CODEC, AttachmentSyncPredicate.all())
		.buildAndRegister(Thaumvibe.id("baubles"));

	public static void init() {
	}
}
