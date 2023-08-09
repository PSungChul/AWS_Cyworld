package com.social.cyworld.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Main {
	private int today, todayTotal;
	private String todayIs, profileImgName, profileText, history, ilchonList, introduceBanner, search, bgmName;
	/////////////// 일촌평 ///////////////
	private int num, ilchonpyeongIdx;
	private String ilchonSession, ilchonpyeongText;
	//파일을 받기 위한 클래스
	private MultipartFile photo;
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// getter / setter
	
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public MultipartFile getPhoto() {
		return photo;
	}
	public void setPhoto(MultipartFile photo) {
		this.photo = photo;
	}
	public int getToday() {
		return today;
	}
	public void setToday(int today) {
		this.today = today;
	}
	public int getTodayTotal() {
		return todayTotal;
	}
	public void setTodayTotal(int todayTotal) {
		this.todayTotal = todayTotal;
	}
	public String getTodayIs() {
		return todayIs;
	}
	public void setTodayIs(String todayIs) {
		this.todayIs = todayIs;
	}
	public String getProfileImgName() {
		return profileImgName;
	}
	public void setProfileImgName(String profileImgName) {
		this.profileImgName = profileImgName;
	}
	public String getProfileText() {
		return profileText;
	}
	public void setProfileText(String profileText) {
		this.profileText = profileText;
	}
	public String getHistory() {
		return history;
	}
	public void setHistory(String history) {
		this.history = history;
	}
	public String getIlchonList() {
		return ilchonList;
	}
	public void setIlchonList(String ilchonList) {
		this.ilchonList = ilchonList;
	}
	public String getIntroduceBanner() {
		return introduceBanner;
	}
	public void setIntroduceBanner(String introduceBanner) {
		this.introduceBanner = introduceBanner;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public String getBgmName() {
		return bgmName;
	}
	public void setBgmName(String bgmName) {
		this.bgmName = bgmName;
	}
	public String getIlchonpyeongText() {
		return ilchonpyeongText;
	}
	public void setIlchonpyeongText(String ilchonpyeongText) {
		this.ilchonpyeongText = ilchonpyeongText;
		}
	public int getIlchonpyeongIdx() {
		return ilchonpyeongIdx;
	}
	public void setIlchonpyeongIdx(int ilchonpyeongIdx) {
		this.ilchonpyeongIdx = ilchonpyeongIdx;
	}
	public String getIlchonSession() {
		return ilchonSession;
	}
	public void setIlchonSession(String ilchonSession) {
		this.ilchonSession = ilchonSession;
	}
}