using CloudWork.Model;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace CloudWork.Common.DB
{
    public class CloudWorkDbContext : DbContext
    {
#pragma warning disable CS8618
        public CloudWorkDbContext(DbContextOptions<CloudWorkDbContext> options) : base(options) { }
#pragma warning restore CS8618

        public DbSet<User> Users { get; set; }
        public DbSet<Question> Questions { get; set; }
        public DbSet<Submission> Submissions { get; set; }
        public DbSet<TestCase> TestCases { get; set; }
        public DbSet<SubmissionResult> SubmissionEvaluations { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.LogTo(Console.WriteLine, LogLevel.Information);
            // optionsBuilder.UseLazyLoadingProxies();
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // 模型约束和关系
            modelBuilder.Entity<User>(entity =>
            {
                entity.HasIndex(u => u.UserName);
                entity.HasIndex(u => u.PhoneNumber);
                entity.Property(u => u.IsDeleted).HasDefaultValue(false);
            });

            modelBuilder.Entity<Question>(entity =>
            {
                entity.Property(p => p.IsPublic).HasDefaultValue(true);
                entity.HasIndex(entity => entity.Title);
                entity.HasMany(q => q.TestCases)
                      .WithOne(tc => tc.Question)
                      .HasForeignKey(tc => tc.QuestionId)
                      .OnDelete(DeleteBehavior.Restrict);
            });

            modelBuilder.Entity<Submission>(entity =>
            {
                entity.HasOne(s => s.User)
                      .WithMany(u => u.Submissions)
                      .HasForeignKey(s => s.UserId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(s => s.Question)
                      .WithMany(p => p.Submissions)
                      .HasForeignKey(s => s.QuestionId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(s => s.Evaluation)
                      .WithOne(se => se.Submission)
                      .HasForeignKey<SubmissionResult>(se => se.SubmissionId)
                      .OnDelete(DeleteBehavior.Restrict);
            });

            modelBuilder.Entity<TestCase>(entity =>
            {
                entity.HasOne(tc => tc.Question)
                      .WithMany(p => p.TestCases)
                      .HasForeignKey(tc => tc.QuestionId);
            });

        }
    }
}