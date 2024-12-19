using CloudWork.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.ChangeTracking;
using System.Collections.Generic;
using System.Reflection.Emit;

namespace CloudWork.Data
{
    public class AppContext : DbContext
    {
        public AppContext(DbContextOptions<AppContext> options) : base(options)
        {
        }

        public DbSet<User> Users { get; set; }
        public DbSet<Problem> Problems { get; set; }
        public DbSet<Submission> Submissions { get; set; }
        public DbSet<TestCase> TestCases { get; set; }
        public DbSet<SubmissionEvaluation> SubmissionEvaluations { get; set; }

        private static readonly DateTime DefaultDateTime =  new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.LogTo(System.Console.WriteLine, LogLevel.Information);
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // 配置额外的模型约束和关系
            // User Configuration
            modelBuilder.Entity<User>(entity =>
            {
                entity.HasKey(u => u.Id);
                entity.Property(u => u.Username).IsRequired().IsUnicode(false);
                entity.Property(u => u.PasswordHash).IsRequired();
                entity.HasIndex(u => u.Username).IsUnique();
            });

            // Problem Configuration
            modelBuilder.Entity<Problem>(entity =>
            {
                entity.HasKey(p => p.Id);
                entity.Property(p => p.Title).IsRequired().IsUnicode(false);
                entity.Property(p => p.Description).IsRequired();
            });

            // Submission Configuration
            modelBuilder.Entity<Submission>(entity =>
            {
                entity.HasKey(s => s.Id);
                entity.Property(s => s.Code).IsRequired();
                entity.Property(s => s.SubmittedAt).HasDefaultValue(DefaultDateTime);

                entity.HasOne(s => s.User)
                      .WithMany(u => u.Submissions)
                      .HasForeignKey(s => s.UserId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(s => s.Problem)
                      .WithMany(p => p.Submissions)
                      .HasForeignKey(s => s.ProblemId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(s => s.Evaluation)
                      .WithOne(se => se.Submission)
                      .HasForeignKey<SubmissionEvaluation>(se => se.SubmissionId);
            });

            // TestCase Configuration
            modelBuilder.Entity<TestCase>(entity =>
            {
                entity.HasKey(tc => tc.Id);
                entity.Property(tc => tc.Input).IsRequired();
                entity.Property(tc => tc.ExpectedOutput).IsRequired();

                entity.HasOne(tc => tc.Problem)
                      .WithMany(p => p.TestCases)
                      .HasForeignKey(tc => tc.ProblemId);
            });

            // SubmissionEvaluation Configuration
            modelBuilder.Entity<SubmissionEvaluation>(entity =>
            {
                entity.HasKey(se => se.Id);
                entity.Property(se => se.EvaluatedAt).HasDefaultValue(DefaultDateTime);
            });
        }
    }
}