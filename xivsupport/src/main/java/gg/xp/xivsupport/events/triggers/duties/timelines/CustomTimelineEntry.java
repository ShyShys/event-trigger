package gg.xp.xivsupport.events.triggers.duties.timelines;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

// Just going to use jackson for now
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomTimelineEntry implements TimelineEntry {

	// TODO: encapsulate these better
	public double time;
	public @Nullable String name;
	public @Nullable Pattern sync;
	public @Nullable Double duration;
	public @Nullable Double windowStart;
	public @Nullable Double windowEnd;
	public @Nullable Double jump;
	public @Nullable URL icon;
	private @Nullable TimelineReference replaces;

	public CustomTimelineEntry() {
		name = "Name Goes Here";
	}

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public CustomTimelineEntry(
			@JsonProperty("time") double time,
			@JsonProperty("name") @Nullable String name,
			@JsonProperty("sync") @Nullable Pattern sync,
			@JsonProperty("duration") @Nullable Double duration,
			@JsonProperty("timelineWindow") @NotNull TimelineWindow timelineWindow,
			@JsonProperty("jump") @Nullable Double jump,
			@JsonProperty("icon") @Nullable URL icon,
			@JsonProperty("replaces") @Nullable TimelineReference replaces) {
		this.time = time;
		this.name = name;
		this.sync = sync;
		this.duration = duration;
		this.windowStart = timelineWindow.start();
		this.windowEnd = timelineWindow.end();
		this.jump = jump;
		this.icon = icon;
		this.replaces = replaces;
	}

	@Override
	@JsonProperty
	public double time() {
		return time;
	}

	@Override
	@JsonProperty
	public @Nullable String name() {
		return name;
	}

	@Override
	@JsonProperty
	public @Nullable Pattern sync() {
		return sync;
	}

	@Override
	@JsonProperty
	public @Nullable Double duration() {
		return duration;
	}

	@Override
	@JsonProperty
	public @NotNull TimelineWindow timelineWindow() {
		Double start = windowStart;
		Double end = windowEnd;
		if (start == null) {
			start = 2.5;
		}
		if (end == null) {
			end = 2.5;
		}
		return new TimelineWindow(start, end);
	}

	@Override
	@JsonProperty
	public @Nullable Double jump() {
		return jump;
	}

	@Override
	@JsonProperty
	public URL icon() {
		return icon;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CustomTimelineEntry that = (CustomTimelineEntry) o;
		Pattern thisSync = sync();
		Pattern thatSync = that.sync();
		return Objects.equals(time(), that.time())
				&& Objects.equals(name(), that.name())
				&& Objects.equals(thisSync == null ? null : thisSync.pattern(), thatSync == null ? null : thatSync.pattern())
				&& Objects.equals(jump(), that.jump())
				&& Objects.equals(duration(), that.duration())
				&& Objects.equals(icon(), that.icon())
				&& Objects.equals(timelineWindow(), that.timelineWindow());
	}

	@Override
	public int hashCode() {
		return Objects.hash(time, name, sync, duration, windowStart, windowEnd, jump, icon);
	}

	@JsonProperty
	@Override
	public @Nullable TimelineReference replaces() {
		return replaces;
	}

	public static CustomTimelineEntry overrideFor(TimelineEntry other) {
		return new CustomTimelineEntry(
				other.time(),
				other.name(),
				other.sync(),
				other.duration(),
				other.timelineWindow(),
				other.jump(),
				other.icon(),
				new TimelineReference(other.time(), other.name())
		);
	}
}