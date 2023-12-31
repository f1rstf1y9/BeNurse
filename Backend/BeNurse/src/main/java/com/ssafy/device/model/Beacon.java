package com.ssafy.device.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "BEACON")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Beacon {

	@Id
	@Column(name = "ID")
	private String ID;
	
	@Column(name = "LOCATION")
	private String location;
	
	@Column(name = "FLOOR")
	private long floor;
	
	@Column(name = "HOSPITAL_ID")
	private long hospitalID;
}
