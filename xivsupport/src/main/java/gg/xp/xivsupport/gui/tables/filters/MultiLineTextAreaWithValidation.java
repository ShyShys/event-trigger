package gg.xp.xivsupport.gui.tables.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MultiLineTextAreaWithValidation<X> extends JTextArea {

	private static final Logger log = LoggerFactory.getLogger(MultiLineTextAreaWithValidation.class);

	protected final Color originalBackground;
	private final Function<String, X> parser;
	private final Consumer<X> consumer;
	private final Consumer<InputValidationState> validationUpdatedCallback;
	protected final Color invalidBackground = new Color(62, 27, 27);
	private boolean stopUpdate;
	private InputValidationState state = InputValidationState.INITIAL;

	public MultiLineTextAreaWithValidation(Function<String, X> parser, Consumer<X> consumer, String initialValue, Consumer<InputValidationState> validationUpdatedCallback) {
		super(30, 30);
		this.parser = parser;
		this.consumer = consumer;
		this.validationUpdatedCallback = validationUpdatedCallback;
		setText(initialValue);
		setEditable(true);
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}
		});
		originalBackground = getBackground();
		updateValidationCallback();
	}

	private void updateValidationCallback() {
		validationUpdatedCallback.accept(state);
	}


	private void update() {
		boolean validationError;
		String currentRawText = getText();
		try {
			X rawText = parser.apply(currentRawText);
			try {
				consumer.accept(rawText);
				validationError = false;
			} catch (Throwable e) {
				log.error("Error consuming new value ({})", rawText, e);
				validationError = true;
			}
		}
		catch (Throwable e) {
			validationError = true;
		}
		if (validationError) {
			setBackground(invalidBackground);
			state = InputValidationState.INVALID;
		}
		else {
			setBackground(originalBackground);
			state = InputValidationState.VALID;
		}
		updateValidationCallback();
	}

	public InputValidationState getValidationState() {
		return state;
	}
}
