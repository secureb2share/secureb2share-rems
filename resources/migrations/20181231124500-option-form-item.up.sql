-- :disable-transaction
ALTER TYPE itemtype
  ADD VALUE 'option';
--;;
CREATE TABLE application_form_item_options
(
  itemId       integer      NOT NULL,
  key          varchar(255) NOT NULL,
  langCode     varchar(64)  NOT NULL,
  label        varchar(255) NOT NULL,
  displayOrder integer      NOT NULL,
  PRIMARY KEY (itemId, key, langCode),
  FOREIGN KEY (itemId) REFERENCES application_form_item (id)
);
