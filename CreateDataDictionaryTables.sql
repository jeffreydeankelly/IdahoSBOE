/** 
*   Script to create database tables for
*   Idaho State Board Of Education
*   Online Data Dictionary
*   
*   Author:	jeffk
*   Date:	Jan. 17, 2014
**/

CREATE TABLE GlossaryType (
	GlossType		VARCHAR(30)		NOT NULL, 
	GlossDesc		VARCHAR(100)	NOT NULL,
	GlossVisible	CHAR(1)			NOT NULL,
	CONSTRAINT PkGlossType PRIMARY KEY (GlossType)
)
GO

CREATE TABLE Glossary (
	GlossType		VARCHAR(30)		NOT NULL, 
	ItemName		VARCHAR(80)		NOT NULL, 
	ItemNarrative	VARCHAR(4000)	NOT NULL, 
	CreateUserId	VARCHAR(30), 
	UpdateUserId	VARCHAR(30), 
	EffDateStart	DATE, 
	EffDateEnd		DATE, 
	CreateDate		DATE, 
	UpdateDate		DATE, 
	CONSTRAINT PkGloss PRIMARY KEY (GlossType, ItemName)
)
GO

ALTER TABLE Glossary
   ADD CONSTRAINT FkGlossaryGlossType FOREIGN KEY (GlossType)
      REFERENCES GlossaryType (GlossType)
GO

CREATE TABLE GlossaryNarrIndx (
	GlossType		VARCHAR(30)	NOT NULL, 
	ItemName		VARCHAR(80)	NOT NULL, 
	Word			VARCHAR(80)	NOT NULL, 
	CreateUserId	VARCHAR(30)	NOT NULL, 
	CreateDate		DATE		NOT NULL, 
	CONSTRAINT PkGlossNarrIndx PRIMARY KEY (GlossType, ItemName, Word)
)
GO

ALTER TABLE GlossaryNarrIndx
   ADD CONSTRAINT FkGlossNarrIndxGlossType FOREIGN KEY (GlossType)
      REFERENCES GlossaryType (GlossType)
GO

CREATE TABLE GlossaryHistory (
	Id	 			INT				NOT NULL,
	GlossType		VARCHAR(30)		NOT NULL, 
	DeleteUserId	VARCHAR(30), 
	CreateUserId	VARCHAR(30)		NOT NULL, 
	UpdateDate		DATE, 
	ItemNarrative	VARCHAR(4000)	NOT NULL, 
	DeleteDate		DATE, 
	EffDateStart	DATE			NOT NULL, 
	UpdateUserId	VARCHAR(30), 
	CreateDate		DATE			NOT NULL, 
	EffDateEnd		DATE, 
	ItemName		VARCHAR(80)		NOT NULL, 
	CONSTRAINT PkGlossHist PRIMARY KEY (Id)
)
GO

ALTER TABLE GlossaryHistory
   ADD CONSTRAINT FkGlossHistGlossType FOREIGN KEY (GlossType)
      REFERENCES GlossaryType (GlossType)
GO

CREATE TABLE GlossaryHistoryNarrIndx (
	Id				INT			IDENTITY(1,1) NOT NULL,
	Word			VARCHAR(80) NOT NULL,
	CreateUserId	VARCHAR(30) NOT NULL, 
	CreateDate		DATE NOT NULL, 
	CONSTRAINT PkGlossHistNarrIndx PRIMARY KEY (Id, Word)
)
GO

CREATE TABLE GlossaryXwalk (
	CreateUserId	VARCHAR(30)	NOT NULL, 
	CreateDate		DATE		NOT NULL, 
	ItemNameParent	VARCHAR(80)	NOT NULL, 
	ItemNameChild	VARCHAR(80)	NOT NULL, 
	GlossTypeParent	VARCHAR(30)	NOT NULL, 
	GlossTypeChild	VARCHAR(30)	NOT NULL, 
	CONSTRAINT PkGlossXwalk PRIMARY KEY (ItemNameParent, 
											ItemNameChild, 
											GlossTypeParent, 
											GlossTypeChild)
)
GO

CREATE TABLE GlossaryXwalkHistory (
	Id				INT			NOT NULL, 
	GlossTypeParent	VARCHAR(30)	NOT NULL, 
	DeleteDate		DATE, 
	ItemNameParent	VARCHAR(80)	NOT NULL, 
	CreateUserId	VARCHAR(30)	NOT NULL, 
	ItemNameChild	VARCHAR(80)	NOT NULL, 
	GlossTypeChild	VARCHAR(30)	NOT NULL, 
	DeleteUserId	VARCHAR(30), 
	CreateDate		DATE		NOT NULL, 
	CONSTRAINT PkGlossXwalkHist PRIMARY KEY (Id)
)
GO

CREATE TABLE GlossaryWordAlias (
	WordAlais	VARCHAR(80) NOT NULL, 
	Word		VARCHAR(80) NOT NULL, 
	CONSTRAINT PkGlossWordAlias PRIMARY KEY (WordAlais, Word)
)
GO

CREATE TABLE GlossaryUsageStatistics (
	GlossaryUsagePage		VARCHAR(1) NOT NULL,
	GlossaryUsageExtra		VARCHAR(200),
	GlossaryUsageIP			VARCHAR(16) NOT NULL,
	GlossaryUsageSession	VARCHAR(256) NOT NULL,
	GlossaryUsageTimestamp	DATETIME NOT NULL
)
GO


CREATE TABLE GlossaryNoiseWord (
	Word VARCHAR(80) NOT NULL, 
	CONSTRAINT PkGlossNoiseWord PRIMARY KEY (Word)
)
GO

CREATE TABLE DictUser (
	UserId			VARCHAR(30) NOT NULL, 
	UserPassword	VARCHAR(15) NOT NULL, 
	UserDesc		VARCHAR(400), 
	UserName		VARCHAR(50) NOT NULL, 
	CONSTRAINT PkDictUser PRIMARY KEY (UserId)
)
GO

CREATE TABLE KeySequence (
	SEQ_NAME		VARCHAR(50) NOT NULL, 
	SEQ_COUNT	DECIMAL, 
	CONSTRAINT PkKeySequence PRIMARY KEY (SEQ_NAME)
)
GO

INSERT INTO KeySequence(SEQ_NAME, SEQ_COUNT) VALUES ('GlossaryXwalkHistorySeq', 0);
INSERT INTO KeySequence(SEQ_NAME, SEQ_COUNT) VALUES ('GlossaryHistorySeq', 0);
GO
