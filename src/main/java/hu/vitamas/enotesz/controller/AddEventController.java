/*
 * Copyright (C) 2017 Vincze Tamas Zoltan (www.vitamas.hu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.vitamas.enotesz.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.vitamas.enotesz.dao.EventsDao;
import hu.vitamas.enotesz.model.Events;
import hu.vitamas.enotesz.model.Users;
import hu.vitamas.enotesz.util.Auth;
import hu.vitamas.enotesz.util.EventFormValidator;
import hu.vitamas.enotesz.view.Alerts;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller class of the event/add.fxml.
 * 
 * @author vitozy
 *
 */
public class AddEventController implements Initializable {

	static Logger logger = LoggerFactory.getLogger(AddEventController.class);

	@FXML
	private AnchorPane AnchorPane;
	@FXML
	private HTMLEditor eventText;
	@FXML
	private TextField eventName;
	@FXML
	private TextField eventPlace;
	@FXML
	private DatePicker eventFromDate;
	@FXML
	private DatePicker eventToDate;
	@FXML
	private TextField eventFromTime;
	@FXML
	private TextField eventToTime;
	@FXML
	private Button eventSaveBtn;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		eventFromDate.setValue(LocalDate.now());
		eventToDate.setValue(LocalDate.now());
		eventFromTime.setText("00:00");
		eventToTime.setText("23:59");

		eventSaveBtn.setOnMouseClicked(this::save);
		
		eventFromDate.valueProperty().addListener((ov, oldValue, newValue) -> {
			if(newValue != null) {
				LocalDate to = eventToDate.getValue();
				if(to == null || to.isBefore(newValue))
					eventToDate.setValue(newValue);
			}
        });
		
		eventToDate.valueProperty().addListener((ov, oldValue, newValue) -> {
			if(newValue != null) {
				LocalDate from = eventFromDate.getValue();
				if(from == null || from.isAfter(newValue))
					eventFromDate.setValue(newValue);
			}
        });
	}

	private void save(MouseEvent e) {
		EventsDao evdao = new EventsDao();
		Events ev = new Events();

		Boolean valid = EventFormValidator.newValidator().checkName(eventName.getText())
				.checkDates(eventFromDate.getValue(), eventToDate.getValue())
				.checkTimes(eventFromTime.getText(), eventToTime.getText()).isValid();

		Users user = Auth.getUser();

		if (valid && user != null) {
			ev.setTitle(eventName.getText());
			ev.setPlace(eventPlace.getText());
			ev.setDateFrom(eventFromDate.getValue());
			ev.setDateTo(eventToDate.getValue());
			ev.setTimeFrom(LocalTime.parse(eventFromTime.getText()));
			ev.setTimeTo(LocalTime.parse(eventToTime.getText()));

			String st = eventText.getHtmlText();
			if (st.contains("contenteditable=\"true\"")) {
				st = st.replace("contenteditable=\"true\"", "contenteditable=\"false\"");
			}
			ev.setText(st);
			ev.setUsers(user);
			
			evdao.create(ev);

			Alert alert = Alerts.success("Sikeresen létrehozva!");
			alert.setOnCloseRequest(creq -> closeWindow(e));
			alert.showAndWait().ifPresent(response -> closeWindow(e));
		} else {
			Alerts.warning("Nem megfelelő adatok!").show();
		}
	}

	private void closeWindow(MouseEvent e) {
		Node n = (Node) (e.getSource());
		Stage stg = (Stage) n.getScene().getWindow();
		stg.fireEvent(new WindowEvent(stg, WindowEvent.WINDOW_CLOSE_REQUEST));
		stg.close();
	};

}
