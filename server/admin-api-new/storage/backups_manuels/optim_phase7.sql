ALTER TABLE `Reservation` DROP INDEX `idx_idNumber`;

ALTER TABLE `Loand` ADD INDEX `idx_loand_agent_giver` (`idAgentGiver`);
ALTER TABLE `Loand` ADD INDEX `idx_loand_agent_recover` (`idAgentRecover`);
ALTER TABLE `invitation_logs` ADD INDEX `idx_invitation_agent_id` (`agent_id`);
ALTER TABLE `invitation_logs` ADD INDEX `idx_invitation_generated_code` (`generated_code`);
ALTER TABLE `invitation_logs` ADD INDEX `idx_invitation_generated_by` (`generated_by`);

ALTER TABLE `Structure` MODIFY COLUMN `storageLimit` BIGINT UNSIGNED DEFAULT 16106100000;
