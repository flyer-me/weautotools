using CloudWork.Common;
using CloudWork.Common.DB;
using CloudWork.Model;
using CloudWork.Repository.Base;
using Microsoft.EntityFrameworkCore.Storage;

namespace CloudWork.Repository.UnitOfWork
{
    [Service]
    public class UnitOfWork : IUnitOfWork
    {
        public CloudWorkDbContext _context;
        private IDbContextTransaction? _transaction;

        private TestCaseRepository? _testCases;
        private BaseRepository<Question>? _questions;
        private BaseRepository<User>? _users;
        private BaseRepository<Submission>? _submissions;
        private BaseRepository<SubmissionResult>? _submissionResults;

        public UnitOfWork(CloudWorkDbContext context)
        {
            _context = context;
        }

        public void Begin()
        {
            _transaction = _context.Database.BeginTransaction();
        }

        public void Commit()
        {
            _transaction?.Commit();
        }

        public void Rollback()
        {
            _transaction?.Rollback();
            _transaction?.Dispose();
        }

        public async Task<int> SaveAsync()
        {
            return await _context.SaveChangesAsync();
        }

        public void Dispose()
        {
            _context.Dispose();
        }

        public TestCaseRepository TestCases => _testCases ??= new TestCaseRepository(_context);
        public BaseRepository<Question> Questions => _questions ??= new QuestionRepository(_context);
        public BaseRepository<User> Users => _users ??= new BaseRepository<User>(_context);
        public BaseRepository<Submission> Submissions => _submissions ??= new BaseRepository<Submission>(_context);
        public BaseRepository<SubmissionResult> SubmissionEvaluations => _submissionResults ??= new BaseRepository<SubmissionResult>(_context);

    }
}
