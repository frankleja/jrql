package com.hlcl.rql.as;

/**
 * Helper definitions for working with Detailed Authorization Packages and the like.
 */
public interface Authorization {


	/**
	 * Designate which of the magic attributes to use
	 * 
	 * @return an integer betreen 1 and 8, 0 for implementation bugs.
	 */
	int getOffset();
	int getBitmask();

	
	/**
	 * Whoever said there is no code in interfaces...
	 */
	public static class Fun {
		public static boolean equals(Authorization l, Authorization r) {
			return l.getOffset() == r.getOffset() && l.getBitmask() == r.getBitmask();
		}
	}
	
	
	
	// 11_2_SP2/10334.htm "Authorization Flag Values for Structural Elements"
	public enum StructureElement implements Authorization {
		ReadLinkElement(2, 8388608), // Undocumented, FIXME: Test again	
		EditLinkProperties(2, 0), // FIXME: Docs say (2, 2) Test again 		
		CreatePages(2, 2), // FIXME: Undocumented, Create pages on a structural element
		ConnectToExistingPages(2, 4),		
		CopyAndConnectPagesFromClipboard(2, 8), // Connect to structural elements in clipboard
		ReferencePage(2, 16777216), // FIXME: Documented as (2, 16)
		ReferenceLink(2, 16), // FIXME: Test again 		
		DeleteReference(2, 32), // Delete references		
		AssignURL(2, 64),		
		AssignAuthorizations(2, 128),		
		DefineVariants(2, 256),		
		DefineWorkflow(2, 512),		
		ConnectWorkflowFromClipboard(2, 1024), // Connect to workflow in clipboard		
		EditTargetContainer(2, 2048),		
		AssignTargetContainer(2, 4096),		
		EditPublicationSettings(2, 16384),		
		PublishPages(2, 32768),		
		ExpandOrDeleteDynamicLinks(2, 65536),
		EditDynamicLinkOrder(2, 131072), // Edit order of dynamic links		
		EditSortOrderSettings(2, 262144),
		EditKeywordAssignment(2, 524288), // Edit key word linking		
		ShowReferenceList(2, 2097152),
		RemoveItemsFromList(2, 4194304), // Undocumented		
// 	Full control: Slot2Bit0, Slot2Bit13, Slot2Bit20, Slot2Bit22, Slot2Bit23, Slot2Bit24
		;
		
		StructureElement(int offset, int bitmask) {
			this.offset = offset;
			this.bitmask = bitmask;
		}

		int offset;
		int bitmask;

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public int getBitmask() {
			return bitmask;
		}
	}
	
	
	public enum Page implements Authorization {
		// * Pages and Instances (in general)
		Read(1, 16),		
		Delete(1, 256),
		// Full control: Read / Delete / ShowInformation / Slot1Bit1

		// * Pages and Instances (in detail)
		EditHeadlines(1, 1),		
		EditFileNames(1, 4),		
		EditDatabaseQuery(1, 8),		
		LinkiningOrAppearanceSchedule(1, 32),		
		AssignKeywords(1, 64),		
		AssignAuthorizationPackage(1, 128),		
		DisconnectPagesFromLink(1, 512), // Disconnect page from structural element		
		DisplayContentClassInTree(1, 1024),		
		SetSmartEditStartPage(1, 2048),
		ReleasePages(1, 4096),		
		PublishPages(1, 8192),		
		EditNotes(1, 262144),		
		ConnectToLinksInClipboard(1, 2097152), // Undocumented: 1, Bit 21
		ShowInformation(1, 4194304), // FIXME: Test again		
		ShowReferenceList(1, 8388608),		
		ShowVersions(1, 16777216),		
		SubmitForTranslation(1, 536870912)		
		// Full control: All of the above, plus Slot1Bit19, Slot1Bit20, Slot1Bit21, Slot1Bit22, FIXME: Test again
		;
		
		Page(int offset, int bitmask) {
			this.offset = offset;
			this.bitmask = bitmask;
		}

		int offset;
		int bitmask;

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public int getBitmask() {
			return bitmask;
		}
	}
	
	
	public enum Dummy implements Authorization {
		Slot1Bit0(1, 0b1),
		Slot1Bit1(1, 0b10),
		Slot1Bit2(1, 0b100),
		Slot1Bit3(1, 0b1000),
		Slot1Bit4(1, 0b10000),
		Slot1Bit5(1, 0b100000),
		Slot1Bit6(1, 0b1000000),
		Slot1Bit7(1, 0b10000000),
		Slot1Bit8(1, 0b100000000),
		Slot1Bit9(1, 0b1000000000),
		Slot1Bit10(1, 0b10000000000),
		Slot1Bit11(1, 0b100000000000),
		Slot1Bit12(1, 0b1000000000000),
		Slot1Bit13(1, 0b10000000000000),
		Slot1Bit14(1, 0b100000000000000),
		Slot1Bit15(1, 0b1000000000000000),
		Slot1Bit16(1, 0b10000000000000000),
		Slot1Bit17(1, 0b100000000000000000),
		Slot1Bit18(1, 0b1000000000000000000),
		Slot1Bit19(1, 0b10000000000000000000),
		Slot1Bit20(1, 0b100000000000000000000),
		Slot1Bit21(1, 0b1000000000000000000000),
		Slot1Bit22(1, 0b10000000000000000000000),
		Slot1Bit23(1, 0b100000000000000000000000),
		Slot1Bit24(1, 0b1000000000000000000000000),
		Slot1Bit25(1, 0b10000000000000000000000000),
		Slot1Bit26(1, 0b100000000000000000000000000),
		Slot1Bit27(1, 0b1000000000000000000000000000),
		Slot1Bit28(1, 0b10000000000000000000000000000),
		Slot1Bit29(1, 0b100000000000000000000000000000),
		Slot1Bit30(1, 0b1000000000000000000000000000000),
		Slot1Bit31(1, 0b10000000000000000000000000000000),

		Slot2Bit0(2, 0b1),
		Slot2Bit1(2, 0b10),
		Slot2Bit2(2, 0b100),
		Slot2Bit3(2, 0b1000),
		Slot2Bit4(2, 0b10000),
		Slot2Bit5(2, 0b100000),
		Slot2Bit6(2, 0b1000000),
		Slot2Bit7(2, 0b10000000),
		Slot2Bit8(2, 0b100000000),
		Slot2Bit9(2, 0b1000000000),
		Slot2Bit10(2, 0b10000000000),
		Slot2Bit11(2, 0b100000000000),
		Slot2Bit12(2, 0b1000000000000),
		Slot2Bit13(2, 0b10000000000000),
		Slot2Bit14(2, 0b100000000000000),
		Slot2Bit15(2, 0b1000000000000000),
		Slot2Bit16(2, 0b10000000000000000),
		Slot2Bit17(2, 0b100000000000000000),
		Slot2Bit18(2, 0b1000000000000000000),
		Slot2Bit19(2, 0b10000000000000000000),
		Slot2Bit20(2, 0b100000000000000000000),
		Slot2Bit21(2, 0b1000000000000000000000),
		Slot2Bit22(2, 0b10000000000000000000000),
		Slot2Bit23(2, 0b100000000000000000000000),
		Slot2Bit24(2, 0b1000000000000000000000000),
		Slot2Bit25(2, 0b10000000000000000000000000),
		Slot2Bit26(2, 0b100000000000000000000000000),
		Slot2Bit27(2, 0b1000000000000000000000000000),
		Slot2Bit28(2, 0b10000000000000000000000000000),
		Slot2Bit29(2, 0b100000000000000000000000000000),
		Slot2Bit30(2, 0b1000000000000000000000000000000),
		Slot2Bit31(2, 0b10000000000000000000000000000000),

		Slot3Bit0(3, 0b1),
		Slot3Bit1(3, 0b10),
		Slot3Bit2(3, 0b100),
		Slot3Bit3(3, 0b1000),
		Slot3Bit4(3, 0b10000),
		Slot3Bit5(3, 0b100000),
		Slot3Bit6(3, 0b1000000),
		Slot3Bit7(3, 0b10000000),
		Slot3Bit8(3, 0b100000000),
		Slot3Bit9(3, 0b1000000000),
		Slot3Bit10(3, 0b10000000000),
		Slot3Bit11(3, 0b100000000000),
		Slot3Bit12(3, 0b1000000000000),
		Slot3Bit13(3, 0b10000000000000),
		Slot3Bit14(3, 0b100000000000000),
		Slot3Bit15(3, 0b1000000000000000),
		Slot3Bit16(3, 0b10000000000000000),
		Slot3Bit17(3, 0b100000000000000000),
		Slot3Bit18(3, 0b1000000000000000000),
		Slot3Bit19(3, 0b10000000000000000000),
		Slot3Bit20(3, 0b100000000000000000000),
		Slot3Bit21(3, 0b1000000000000000000000),
		Slot3Bit22(3, 0b10000000000000000000000),
		Slot3Bit23(3, 0b100000000000000000000000),
		Slot3Bit24(3, 0b1000000000000000000000000),
		Slot3Bit25(3, 0b10000000000000000000000000),
		Slot3Bit26(3, 0b100000000000000000000000000),
		Slot3Bit27(3, 0b1000000000000000000000000000),
		Slot3Bit28(3, 0b10000000000000000000000000000),
		Slot3Bit29(3, 0b100000000000000000000000000000),
		Slot3Bit30(3, 0b1000000000000000000000000000000),
		Slot3Bit31(3, 0b10000000000000000000000000000000),

		Slot4Bit0(4, 0b1),
		Slot4Bit1(4, 0b10),
		Slot4Bit2(4, 0b100),
		Slot4Bit3(4, 0b1000),
		Slot4Bit4(4, 0b10000),
		Slot4Bit5(4, 0b100000),
		Slot4Bit6(4, 0b1000000),
		Slot4Bit7(4, 0b10000000),
		Slot4Bit8(4, 0b100000000),
		Slot4Bit9(4, 0b1000000000),
		Slot4Bit10(4, 0b10000000000),
		Slot4Bit11(4, 0b100000000000),
		Slot4Bit12(4, 0b1000000000000),
		Slot4Bit13(4, 0b10000000000000),
		Slot4Bit14(4, 0b100000000000000),
		Slot4Bit15(4, 0b1000000000000000),
		Slot4Bit16(4, 0b10000000000000000),
		Slot4Bit17(4, 0b100000000000000000),
		Slot4Bit18(4, 0b1000000000000000000),
		Slot4Bit19(4, 0b10000000000000000000),
		Slot4Bit20(4, 0b100000000000000000000),
		Slot4Bit21(4, 0b1000000000000000000000),
		Slot4Bit22(4, 0b10000000000000000000000),
		Slot4Bit23(4, 0b100000000000000000000000),
		Slot4Bit24(4, 0b1000000000000000000000000),
		Slot4Bit25(4, 0b10000000000000000000000000),
		Slot4Bit26(4, 0b100000000000000000000000000),
		Slot4Bit27(4, 0b1000000000000000000000000000),
		Slot4Bit28(4, 0b10000000000000000000000000000),
		Slot4Bit29(4, 0b100000000000000000000000000000),
		Slot4Bit30(4, 0b1000000000000000000000000000000),
		Slot4Bit31(4, 0b10000000000000000000000000000000),

		Slot5Bit0(5, 0b1),
		Slot5Bit1(5, 0b10),
		Slot5Bit2(5, 0b100),
		Slot5Bit3(5, 0b1000),
		Slot5Bit4(5, 0b10000),
		Slot5Bit5(5, 0b100000),
		Slot5Bit6(5, 0b1000000),
		Slot5Bit7(5, 0b10000000),
		Slot5Bit8(5, 0b100000000),
		Slot5Bit9(5, 0b1000000000),
		Slot5Bit10(5, 0b10000000000),
		Slot5Bit11(5, 0b100000000000),
		Slot5Bit12(5, 0b1000000000000),
		Slot5Bit13(5, 0b10000000000000),
		Slot5Bit14(5, 0b100000000000000),
		Slot5Bit15(5, 0b1000000000000000),
		Slot5Bit16(5, 0b10000000000000000),
		Slot5Bit17(5, 0b100000000000000000),
		Slot5Bit18(5, 0b1000000000000000000),
		Slot5Bit19(5, 0b10000000000000000000),
		Slot5Bit20(5, 0b100000000000000000000),
		Slot5Bit21(5, 0b1000000000000000000000),
		Slot5Bit22(5, 0b10000000000000000000000),
		Slot5Bit23(5, 0b100000000000000000000000),
		Slot5Bit24(5, 0b1000000000000000000000000),
		Slot5Bit25(5, 0b10000000000000000000000000),
		Slot5Bit26(5, 0b100000000000000000000000000),
		Slot5Bit27(5, 0b1000000000000000000000000000),
		Slot5Bit28(5, 0b10000000000000000000000000000),
		Slot5Bit29(5, 0b100000000000000000000000000000),
		Slot5Bit30(5, 0b1000000000000000000000000000000),
		Slot5Bit31(5, 0b10000000000000000000000000000000),

		Slot6Bit0(6, 0b1),
		Slot6Bit1(6, 0b10),
		Slot6Bit2(6, 0b100),
		Slot6Bit3(6, 0b1000),
		Slot6Bit4(6, 0b10000),
		Slot6Bit5(6, 0b100000),
		Slot6Bit6(6, 0b1000000),
		Slot6Bit7(6, 0b10000000),
		Slot6Bit8(6, 0b100000000),
		Slot6Bit9(6, 0b1000000000),
		Slot6Bit10(6, 0b10000000000),
		Slot6Bit11(6, 0b100000000000),
		Slot6Bit12(6, 0b1000000000000),
		Slot6Bit13(6, 0b10000000000000),
		Slot6Bit14(6, 0b100000000000000),
		Slot6Bit15(6, 0b1000000000000000),
		Slot6Bit16(6, 0b10000000000000000),
		Slot6Bit17(6, 0b100000000000000000),
		Slot6Bit18(6, 0b1000000000000000000),
		Slot6Bit19(6, 0b10000000000000000000),
		Slot6Bit20(6, 0b100000000000000000000),
		Slot6Bit21(6, 0b1000000000000000000000),
		Slot6Bit22(6, 0b10000000000000000000000),
		Slot6Bit23(6, 0b100000000000000000000000),
		Slot6Bit24(6, 0b1000000000000000000000000),
		Slot6Bit25(6, 0b10000000000000000000000000),
		Slot6Bit26(6, 0b100000000000000000000000000),
		Slot6Bit27(6, 0b1000000000000000000000000000),
		Slot6Bit28(6, 0b10000000000000000000000000000),
		Slot6Bit29(6, 0b100000000000000000000000000000),
		Slot6Bit30(6, 0b1000000000000000000000000000000),
		Slot6Bit31(6, 0b10000000000000000000000000000000),

		Slot7Bit0(7, 0b1),
		Slot7Bit1(7, 0b10),
		Slot7Bit2(7, 0b100),
		Slot7Bit3(7, 0b1000),
		Slot7Bit4(7, 0b10000),
		Slot7Bit5(7, 0b100000),
		Slot7Bit6(7, 0b1000000),
		Slot7Bit7(7, 0b10000000),
		Slot7Bit8(7, 0b100000000),
		Slot7Bit9(7, 0b1000000000),
		Slot7Bit10(7, 0b10000000000),
		Slot7Bit11(7, 0b100000000000),
		Slot7Bit12(7, 0b1000000000000),
		Slot7Bit13(7, 0b10000000000000),
		Slot7Bit14(7, 0b100000000000000),
		Slot7Bit15(7, 0b1000000000000000),
		Slot7Bit16(7, 0b10000000000000000),
		Slot7Bit17(7, 0b100000000000000000),
		Slot7Bit18(7, 0b1000000000000000000),
		Slot7Bit19(7, 0b10000000000000000000),
		Slot7Bit20(7, 0b100000000000000000000),
		Slot7Bit21(7, 0b1000000000000000000000),
		Slot7Bit22(7, 0b10000000000000000000000),
		Slot7Bit23(7, 0b100000000000000000000000),
		Slot7Bit24(7, 0b1000000000000000000000000),
		Slot7Bit25(7, 0b10000000000000000000000000),
		Slot7Bit26(7, 0b100000000000000000000000000),
		Slot7Bit27(7, 0b1000000000000000000000000000),
		Slot7Bit28(7, 0b10000000000000000000000000000),
		Slot7Bit29(7, 0b100000000000000000000000000000),
		Slot7Bit30(7, 0b1000000000000000000000000000000),
		Slot7Bit31(7, 0b10000000000000000000000000000000),

		Slot8Bit0(8, 0b1),
		Slot8Bit1(8, 0b10),
		Slot8Bit2(8, 0b100),
		Slot8Bit3(8, 0b1000),
		Slot8Bit4(8, 0b10000),
		Slot8Bit5(8, 0b100000),
		Slot8Bit6(8, 0b1000000),
		Slot8Bit7(8, 0b10000000),
		Slot8Bit8(8, 0b100000000),
		Slot8Bit9(8, 0b1000000000),
		Slot8Bit10(8, 0b10000000000),
		Slot8Bit11(8, 0b100000000000),
		Slot8Bit12(8, 0b1000000000000),
		Slot8Bit13(8, 0b10000000000000),
		Slot8Bit14(8, 0b100000000000000),
		Slot8Bit15(8, 0b1000000000000000),
		Slot8Bit16(8, 0b10000000000000000),
		Slot8Bit17(8, 0b100000000000000000),
		Slot8Bit18(8, 0b1000000000000000000),
		Slot8Bit19(8, 0b10000000000000000000),
		Slot8Bit20(8, 0b100000000000000000000),
		Slot8Bit21(8, 0b1000000000000000000000),
		Slot8Bit22(8, 0b10000000000000000000000),
		Slot8Bit23(8, 0b100000000000000000000000),
		Slot8Bit24(8, 0b1000000000000000000000000),
		Slot8Bit25(8, 0b10000000000000000000000000),
		Slot8Bit26(8, 0b100000000000000000000000000),
		Slot8Bit27(8, 0b1000000000000000000000000000),
		Slot8Bit28(8, 0b10000000000000000000000000000),
		Slot8Bit29(8, 0b100000000000000000000000000000),
		Slot8Bit30(8, 0b1000000000000000000000000000000),
		Slot8Bit31(8, 0b10000000000000000000000000000000),                    
		;
		
		Dummy(int offset, int bitmask) {
			this.offset = offset;
			this.bitmask = bitmask;
		}

		int offset;
		int bitmask;

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public int getBitmask() {
			return bitmask;
		}
	}
	
}
